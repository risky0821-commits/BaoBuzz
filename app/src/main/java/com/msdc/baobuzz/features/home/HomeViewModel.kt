package com.msdc.baobuzz.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msdc.baobuzz.core.api.FootballRepository
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
import kotlinx.coroutines.delay
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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val footballRepository: FootballRepository,
    private val footballApi: FootballApi,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

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
                .collect { selectedLeagueIds ->
                    if (selectedLeagueIds.isNotEmpty() && _uiState.value !is HomeUiState.Loading) {
                        loadHomeData()
                    }
                }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                val preferences = userPreferencesRepository.getPreferences().first()
                val selectedLeagues = preferences.selectedLeagueIds.mapNotNull { LeagueData.getLeagueById(it) }
                val selectedLeagueIds = selectedLeagues.map { it.id }
                val favoriteTeamIds = preferences.selectedTeamIds.toSet()

                if (selectedLeagueIds.isEmpty()) {
                    _uiState.value = HomeUiState.NoLeaguesSelected
                    return@launch
                }

                delay(150)

                coroutineScope {
                    val liveDeferred = async { footballRepository.getLiveMatches(selectedLeagueIds) }
                    val windowDeferred = async { loadMatchWindow(selectedLeagues) }

                    val (upcoming, recent) = windowDeferred.await()
                    _uiState.value = HomeUiState.Success(
                        liveMatches = liveDeferred.await().favoriteLiveMatchesFirst(favoriteTeamIds),
                        recentTransfers = emptyList(),
                        leagueStandings = emptyList(),
                        selectedLeagues = selectedLeagues,
                        upcomingFixtures = upcoming.favoriteUpcomingFixturesFirst(favoriteTeamIds),
                        recentResults = recent.favoriteRecentResultsFirst(favoriteTeamIds),
                        leagueInsights = emptyList(),
                        topScorers = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "تعذر تحميل المباريات")
            }
        }
    }

    private suspend fun loadMatchWindow(
        leagues: List<League>
    ): Pair<List<UpcomingFixture>, List<RecentResult>> = coroutineScope {
        val today = LocalDate.now()
        val from = today.minusDays(7).toString()
        val to = today.plusDays(21).toString()
        val nowEpochSeconds = System.currentTimeMillis() / 1000L
        val finishedStatuses = setOf("FT", "AET", "PEN")

        val fixtures = leagues.map { league ->
            async {
                runCatching {
                    footballApi.getFixtures(
                        league = league.id,
                        season = league.season,
                        from = from,
                        to = to
                    ).response
                }.getOrDefault(emptyList())
            }
        }.awaitAll().flatten()

        val upcoming = fixtures
            .filter { it.fixture.timestamp >= nowEpochSeconds && it.fixture.status.short !in finishedStatuses }
            .sortedBy { it.fixture.timestamp }
            .take(30)
            .map { it.toUpcomingFixture() }

        val recent = fixtures
            .filter { it.fixture.timestamp < nowEpochSeconds && it.fixture.status.short in finishedStatuses }
            .sortedByDescending { it.fixture.timestamp }
            .take(20)
            .map { it.toRecentResult() }

        upcoming to recent
    }

    fun toggleFollowMatch(matchId: String) {
        viewModelScope.launch { userPreferencesRepository.toggleFollowMatch(matchId) }
    }

    fun retry() = loadHomeData()
    fun refreshData() = loadHomeData()
    fun loadData() = loadHomeData()
}

private fun ApiFixture.toUpcomingFixture(): UpcomingFixture = UpcomingFixture(
    id = fixture.id.toString(),
    homeTeam = teams.home,
    awayTeam = teams.away,
    dateTime = fixture.date,
    venue = fixture.venue.name,
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

private fun List<LiveMatch>.favoriteLiveMatchesFirst(favoriteTeamIds: Set<Int>): List<LiveMatch> =
    sortedByDescending { it.homeTeam.id in favoriteTeamIds || it.awayTeam.id in favoriteTeamIds }

private fun List<UpcomingFixture>.favoriteUpcomingFixturesFirst(favoriteTeamIds: Set<Int>): List<UpcomingFixture> =
    sortedByDescending { it.homeTeam.id in favoriteTeamIds || it.awayTeam.id in favoriteTeamIds }

private fun List<RecentResult>.favoriteRecentResultsFirst(favoriteTeamIds: Set<Int>): List<RecentResult> =
    sortedByDescending { it.homeTeam.id in favoriteTeamIds || it.awayTeam.id in favoriteTeamIds }

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
