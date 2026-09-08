package com.msdc.baobuzz.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msdc.baobuzz.core.data.LeagueData
import com.msdc.baobuzz.core.models.LeagueInsight
import com.msdc.baobuzz.core.models.LeagueStanding
import com.msdc.baobuzz.core.models.LiveMatch
import com.msdc.baobuzz.core.models.PlayerStat
import com.msdc.baobuzz.core.models.RecentResult
import com.msdc.baobuzz.core.models.TransferDetails
import com.msdc.baobuzz.core.models.UpcomingFixture
import com.msdc.baobuzz.interfaces.FootballApi
import com.msdc.baobuzz.models.Fixture as ApiFixture
import com.msdc.baobuzz.models.League
import com.msdc.baobuzz.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val footballApi: FootballApi,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private data class CachedDateFixtures(
        val fetchedAt: Long,
        val fixtures: List<ApiFixture>
    )

    private val riyadhZone = ZoneId.of("Asia/Riyadh")
    private val dateCache = ConcurrentHashMap<String, CachedDateFixtures>()
    private val dateCacheTtlMs = 5 * 60 * 1000L

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val followedMatchIds: StateFlow<Set<String>> =
        userPreferencesRepository
            .getPreferences()
            .map { it.followedMatchIds }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet()
            )

    val selectedLeaguesWithData: StateFlow<List<League>> =
        userPreferencesRepository
            .getPreferences()
            .map { preferences ->
                preferences.selectedLeagueIds.mapNotNull { LeagueData.getLeagueById(it) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        loadHomeData()
        observeUserPreferencesChanges()
    }

    private fun observeUserPreferencesChanges() {
        viewModelScope.launch {
            userPreferencesRepository
                .getPreferences()
                .map { it.selectedLeagueIds }
                .distinctUntilChanged()
                .collect {
                    if (_uiState.value !is HomeUiState.Loading) loadHomeData()
                }
        }
    }

    private fun loadHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                val preferences = userPreferencesRepository.getPreferences().first()
                val selectedLeagues = preferences.selectedLeagueIds.mapNotNull { LeagueData.getLeagueById(it) }
                val favoriteLeagueIds = preferences.selectedLeagueIds.toSet()
                val favoriteTeamIds = preferences.selectedTeamIds.toSet()

                val fixtures = loadVisibleDateFixtures(forceRefresh)
                val nowEpochSeconds = System.currentTimeMillis() / 1000L
                val finishedStatuses = setOf("FT", "AET", "PEN")
                val liveStatuses = setOf("1H", "HT", "2H", "ET", "BT", "P", "SUSP", "INT", "LIVE")

                val live = fixtures
                    .filter { it.fixture.status.short in liveStatuses }
                    .map { it.toLiveMatch() }
                    .sortedWith(matchPriorityComparator(favoriteLeagueIds, favoriteTeamIds))

                val upcoming = fixtures
                    .filter {
                        it.fixture.timestamp >= nowEpochSeconds &&
                            it.fixture.status.short !in finishedStatuses &&
                            it.fixture.status.short !in liveStatuses
                    }
                    .map { it.toUpcomingFixture() }
                    .sortedWith(upcomingPriorityComparator(favoriteLeagueIds, favoriteTeamIds))

                val recent = fixtures
                    .filter {
                        it.fixture.timestamp < nowEpochSeconds &&
                            it.fixture.status.short in finishedStatuses
                    }
                    .map { it.toRecentResult() }
                    .sortedWith(recentPriorityComparator(favoriteLeagueIds, favoriteTeamIds))

                _uiState.value = HomeUiState.Success(
                    liveMatches = live,
                    recentTransfers = emptyList(),
                    leagueStandings = emptyList(),
                    selectedLeagues = selectedLeagues,
                    upcomingFixtures = upcoming,
                    recentResults = recent,
                    leagueInsights = emptyList(),
                    topScorers = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "تعذر تحميل المباريات")
            }
        }
    }

    private suspend fun loadVisibleDateFixtures(forceRefresh: Boolean): List<ApiFixture> = coroutineScope {
        val today = LocalDate.now(riyadhZone)
        val dates = (-2L..2L).map { today.plusDays(it) }
        val nowMs = System.currentTimeMillis()

        dates.map { date ->
            async {
                val key = date.toString()
                val cached = dateCache[key]
                if (!forceRefresh && cached != null && nowMs - cached.fetchedAt < dateCacheTtlMs) {
                    return@async cached.fixtures
                }

                val fetched = runCatching {
                    footballApi.getFixturesByDate(date = key).response
                }.getOrElse {
                    cached?.fixtures ?: emptyList()
                }

                dateCache[key] = CachedDateFixtures(nowMs, fetched)
                fetched
            }
        }.awaitAll().flatten().distinctBy { it.fixture.id }
    }

    fun toggleFollowMatch(matchId: String) {
        viewModelScope.launch { userPreferencesRepository.toggleFollowMatch(matchId) }
    }

    fun retry() = loadHomeData(forceRefresh = true)
    fun refreshData() = loadHomeData(forceRefresh = true)
    fun loadData() = loadHomeData()
}

private fun ApiFixture.toLiveMatch(): LiveMatch = LiveMatch(
    id = fixture.id.toString(),
    homeTeam = teams.home,
    awayTeam = teams.away,
    homeScore = goals.home,
    awayScore = goals.away,
    status = fixture.status.short,
    minute = fixture.status.elapsed,
    leagueId = league.id
)

private fun ApiFixture.toUpcomingFixture(): UpcomingFixture = UpcomingFixture(
    id = fixture.id.toString(),
    homeTeam = teams.home,
    awayTeam = teams.away,
    dateTime = fixture.date,
    venue = fixture.venue.name.orEmpty(),
    round = league.round,
    leagueId = league.id,
    leagueName = league.name
)

private fun ApiFixture.toRecentResult(): RecentResult = RecentResult(
    id = fixture.id.toString(),
    homeTeam = teams.home,
    awayTeam = teams.away,
    homeScore = goals.home ?: 0,
    awayScore = goals.away ?: 0,
    date = fixture.date,
    round = league.round,
    leagueId = league.id,
    leagueName = league.name
)

private fun isFavoriteTeams(homeId: Int, awayId: Int, favoriteTeamIds: Set<Int>) =
    homeId in favoriteTeamIds || awayId in favoriteTeamIds

private fun matchPriorityComparator(
    favoriteLeagueIds: Set<Int>,
    favoriteTeamIds: Set<Int>
): Comparator<LiveMatch> = compareByDescending<LiveMatch> {
    isFavoriteTeams(it.homeTeam.id, it.awayTeam.id, favoriteTeamIds)
}.thenByDescending { it.leagueId in favoriteLeagueIds }

private fun upcomingPriorityComparator(
    favoriteLeagueIds: Set<Int>,
    favoriteTeamIds: Set<Int>
): Comparator<UpcomingFixture> = compareByDescending<UpcomingFixture> {
    isFavoriteTeams(it.homeTeam.id, it.awayTeam.id, favoriteTeamIds)
}.thenByDescending { it.leagueId in favoriteLeagueIds }.thenBy { it.dateTime }

private fun recentPriorityComparator(
    favoriteLeagueIds: Set<Int>,
    favoriteTeamIds: Set<Int>
): Comparator<RecentResult> = compareByDescending<RecentResult> {
    isFavoriteTeams(it.homeTeam.id, it.awayTeam.id, favoriteTeamIds)
}.thenByDescending { it.leagueId in favoriteLeagueIds }.thenByDescending { it.date }

sealed class HomeUiState {
    object Loading : HomeUiState()
    object OnboardingRequired : HomeUiState()
    object NoLeaguesSelected : HomeUiState()

    data class Success(
        val liveMatches: List<LiveMatch>,
        val recentTransfers: List<TransferDetails>,
        val leagueStandings: List<LeagueStanding>,
        val selectedLeagues: List<League>,
        val upcomingFixtures: List<UpcomingFixture> = emptyList(),
        val recentResults: List<RecentResult> = emptyList(),
        val leagueInsights: List<LeagueInsight> = emptyList(),
        val topScorers: List<PlayerStat> = emptyList()
    ) : HomeUiState()

    data class Error(val message: String, val canRetry: Boolean = true) : HomeUiState()
}
