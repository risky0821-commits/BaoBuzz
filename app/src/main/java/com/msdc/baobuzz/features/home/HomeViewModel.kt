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
import com.msdc.baobuzz.models.League
import com.msdc.baobuzz.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val footballRepository: FootballRepository,
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
                preferences.selectedLeagueIds.mapNotNull { leagueId ->
                    LeagueData.getLeagueById(leagueId)
                }
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

                val userPreferences = userPreferencesRepository.getPreferences().first()
                val selectedLeagueIds = userPreferences.selectedLeagueIds
                val favoriteTeamIds = userPreferences.selectedTeamIds.toSet()

                if (selectedLeagueIds.isEmpty()) {
                    _uiState.value = HomeUiState.NoLeaguesSelected
                    return@launch
                }

                delay(150)

                coroutineScope {
                    // Keep the home feed intentionally light: only matches and results.
                    val liveMatchesDeferred = async { footballRepository.getLiveMatches(selectedLeagueIds) }
                    val upcomingFixturesDeferred = async { footballRepository.getUpcomingFixtures(selectedLeagueIds, 20) }
                    val recentResultsDeferred = async { footballRepository.getRecentResults(selectedLeagueIds, 12) }

                    _uiState.value =
                        HomeUiState.Success(
                            liveMatches = liveMatchesDeferred.await().favoriteLiveMatchesFirst(favoriteTeamIds),
                            recentTransfers = emptyList(),
                            leagueStandings = emptyList(),
                            selectedLeagues = selectedLeagueIds.mapNotNull { LeagueData.getLeagueById(it) },
                            upcomingFixtures = upcomingFixturesDeferred.await().favoriteUpcomingFixturesFirst(favoriteTeamIds),
                            recentResults = recentResultsDeferred.await().favoriteRecentResultsFirst(favoriteTeamIds),
                            leagueInsights = emptyList(),
                            topScorers = emptyList()
                        )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "تعذر تحميل المباريات")
            }
        }
    }

    fun toggleFollowMatch(matchId: String) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFollowMatch(matchId)
        }
    }

    fun retry() = loadHomeData()
    fun refreshData() = loadHomeData()
    fun loadData() = loadHomeData()
}

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
