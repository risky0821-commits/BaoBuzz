package com.msdc.baobuzz.features.leagues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msdc.baobuzz.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaguesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val selectedLeagueIds: StateFlow<Set<Int>> =
        userPreferencesRepository
            .getPreferences()
            .map { it.selectedLeagueIds.toSet() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptySet()
            )

    fun toggleLeague(leagueId: Int) {
        viewModelScope.launch {
            val preferences = userPreferencesRepository.getPreferences().first()
            val selected = preferences.selectedLeagueIds.toMutableList()
            if (leagueId in selected) {
                selected.remove(leagueId)
            } else {
                selected.add(leagueId)
            }
            userPreferencesRepository.savePreferences(
                preferences.copy(selectedLeagueIds = selected.distinct())
            )
        }
    }
}
