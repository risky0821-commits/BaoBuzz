package com.msdc.baobuzz.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msdc.baobuzz.core.data.LeagueData
import com.msdc.baobuzz.models.League
import com.msdc.baobuzz.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject
constructor(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val availableLeagues: List<League> = LeagueData.getPopularLeagues()

    init {
        observeUserPreferences()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.getPreferences().collect { preferences ->
                    val selectedLeagues = preferences.selectedLeagueIds.mapNotNull { LeagueData.getLeagueById(it) }
                    _uiState.value = SettingsUiState.Loaded(
                        selectedLeagues = selectedLeagues,
                        notificationsEnabled = preferences.notificationsEnabled,
                        preferredLanguage = preferences.preferredLanguage,
                        isOnboardingCompleted = preferences.isOnboardingCompleted,
                        teamNotifications = preferences.teamNotifications
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "تعذر تحميل الإعدادات")
            }
        }
    }

    fun addLeague(league: League) {
        viewModelScope.launch {
            val current = userPreferencesRepository.getPreferences().first()
            userPreferencesRepository.savePreferences(
                current.copy(selectedLeagueIds = (current.selectedLeagueIds + league.id).distinct())
            )
        }
    }

    fun removeLeague(league: League) {
        viewModelScope.launch {
            val current = userPreferencesRepository.getPreferences().first()
            userPreferencesRepository.savePreferences(
                current.copy(selectedLeagueIds = current.selectedLeagueIds - league.id)
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferencesRepository.getPreferences().first()
            userPreferencesRepository.savePreferences(current.copy(notificationsEnabled = enabled))
        }
    }

    fun toggleTeamNotifications(teamId: Int, enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferencesRepository.getPreferences().first()
            val updated = current.teamNotifications.toMutableMap().apply { put(teamId, enabled) }
            userPreferencesRepository.savePreferences(current.copy(teamNotifications = updated))
        }
    }

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            val current = userPreferencesRepository.getPreferences().first()
            userPreferencesRepository.savePreferences(current.copy(preferredLanguage = languageCode))
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            userPreferencesRepository.clearAllPreferences()
            _uiState.value = SettingsUiState.DataCleared
        }
    }

    fun retry() {
        _uiState.value = SettingsUiState.Loading
        observeUserPreferences()
    }
}

sealed class SettingsUiState {
    object Loading : SettingsUiState()

    data class Loaded(
        val selectedLeagues: List<League>,
        val notificationsEnabled: Boolean,
        val preferredLanguage: String,
        val isOnboardingCompleted: Boolean,
        val teamNotifications: Map<Int, Boolean>
    ) : SettingsUiState()

    data class Error(val message: String) : SettingsUiState()
    object DataCleared : SettingsUiState()
}

enum class SupportedLanguage(val code: String, val displayName: String) {
    ARABIC("ar", "العربية"),
    ENGLISH("en", "English")
}
