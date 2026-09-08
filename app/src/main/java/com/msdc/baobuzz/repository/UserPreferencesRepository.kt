package com.msdc.baobuzz.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.msdc.baobuzz.core.data.PersonalFootballDefaults
import com.msdc.baobuzz.models.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {
    suspend fun savePreferences(preferences: UserPreferences) {
        dataStore.edit { prefs ->
            prefs[SELECTED_LEAGUES] = preferences.selectedLeagueIds.joinToString(",")
            prefs[SELECTED_TEAMS] = preferences.selectedTeamIds.joinToString(",")
            prefs[FOLLOWED_MATCHES] = preferences.followedMatchIds.joinToString(",")
            prefs[TEAM_NOTIFICATIONS] =
                preferences.teamNotifications.entries.joinToString(",") {
                    "${it.key}:${it.value}"
                }
            prefs[ONBOARDING_COMPLETED] = preferences.isOnboardingCompleted
            prefs[PREFERRED_LANGUAGE] = preferences.preferredLanguage
            prefs[NOTIFICATIONS_ENABLED] = preferences.notificationsEnabled
        }
    }

    suspend fun markOnboardingComplete(selectedLeagueIds: List<Int>) {
        dataStore.edit { prefs ->
            prefs[SELECTED_LEAGUES] = selectedLeagueIds.joinToString(",")
            prefs[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun toggleFollowMatch(matchId: String) {
        dataStore.edit { prefs ->
            val followed = prefs[FOLLOWED_MATCHES]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableSet()
                ?: mutableSetOf()

            if (!followed.add(matchId)) {
                followed.remove(matchId)
            }

            prefs[FOLLOWED_MATCHES] = followed.joinToString(",")
        }
    }

    suspend fun clearAllPreferences() {
        dataStore.edit { prefs -> prefs.clear() }
    }

    fun getPreferences(): Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            UserPreferences(
                selectedLeagueIds =
                    prefs[SELECTED_LEAGUES]
                        ?.split(",")
                        ?.mapNotNull { if (it.isBlank()) null else it.toIntOrNull() }
                        ?: listOf(PersonalFootballDefaults.SAUDI_PRO_LEAGUE_ID),
                selectedTeamIds =
                    prefs[SELECTED_TEAMS]
                        ?.split(",")
                        ?.mapNotNull { if (it.isBlank()) null else it.toIntOrNull() }
                        ?: listOf(PersonalFootballDefaults.AL_AHLI_JEDDAH_TEAM_ID),
                followedMatchIds =
                    prefs[FOLLOWED_MATCHES]
                        ?.split(",")
                        ?.filter { it.isNotBlank() }
                        ?.toSet()
                        ?: emptySet(),
                teamNotifications =
                    prefs[TEAM_NOTIFICATIONS]
                        ?.split(",")
                        ?.mapNotNull { entry ->
                            val parts = entry.split(":")
                            if (parts.size != 2) return@mapNotNull null
                            val teamId = parts[0].toIntOrNull() ?: return@mapNotNull null
                            teamId to parts[1].toBoolean()
                        }
                        ?.toMap()
                        ?: mapOf(PersonalFootballDefaults.AL_AHLI_JEDDAH_TEAM_ID to true),
                isOnboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: true,
                preferredLanguage = prefs[PREFERRED_LANGUAGE] ?: "ar",
                notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: true
            )
        }

    companion object {
        val SELECTED_LEAGUES = stringPreferencesKey("selected_leagues")
        val SELECTED_TEAMS = stringPreferencesKey("selected_teams")
        val FOLLOWED_MATCHES = stringPreferencesKey("followed_matches")
        val TEAM_NOTIFICATIONS = stringPreferencesKey("team_notifications")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }
}
