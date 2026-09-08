package com.msdc.baobuzz.core.data

import androidx.compose.ui.graphics.Color
import com.msdc.baobuzz.models.League
import java.time.LocalDate

/**
 * Static data for popular football leagues with real API IDs and logo URLs.
 *
 * Season values are derived at runtime so the app does not get stuck on an old season.
 */
object LeagueData {

    private fun currentSeasonStartYear(today: LocalDate = LocalDate.now()): Int {
        // Most competitions represented here roll over to the new season in Jul/Aug.
        return if (today.monthValue >= 7) today.year else today.year - 1
    }

    fun getPopularLeagues(): List<League> {
        val season = currentSeasonStartYear()

        return listOf(
            League(
                id = 307,
                name = "Saudi Pro League",
                type = "League",
                country = "Saudi-Arabia",
                logo = "https://media.api-sports.io/football/leagues/307.png",
                flag = "https://media.api-sports.io/flags/sa.svg",
                season = season,
                round = null
            ),
            League(
                id = 39,
                name = "Premier League",
                type = "League",
                country = "England",
                logo = "https://media.api-sports.io/football/leagues/39.png",
                flag = "https://media.api-sports.io/flags/gb.svg",
                season = season,
                round = null
            ),
            League(
                id = 140,
                name = "La Liga",
                type = "League",
                country = "Spain",
                logo = "https://media.api-sports.io/football/leagues/140.png",
                flag = "https://media.api-sports.io/flags/es.svg",
                season = season,
                round = null
            ),
            League(
                id = 78,
                name = "Bundesliga",
                type = "League",
                country = "Germany",
                logo = "https://media.api-sports.io/football/leagues/78.png",
                flag = "https://media.api-sports.io/flags/de.svg",
                season = season,
                round = null
            ),
            League(
                id = 135,
                name = "Serie A",
                type = "League",
                country = "Italy",
                logo = "https://media.api-sports.io/football/leagues/135.png",
                flag = "https://media.api-sports.io/flags/it.svg",
                season = season,
                round = null
            ),
            League(
                id = 61,
                name = "Ligue 1",
                type = "League",
                country = "France",
                logo = "https://media.api-sports.io/football/leagues/61.png",
                flag = "https://media.api-sports.io/flags/fr.svg",
                season = season,
                round = null
            )
        )
    }

    /**
     * Extended league information for onboarding UI.
     */
    data class OnboardingLeague(
        val league: League,
        val primaryColor: Color,
        val description: String,
        val isPopular: Boolean = true
    )

    fun getOnboardingLeagues(): List<OnboardingLeague> {
        val leagues = getPopularLeagues()

        return listOf(
            OnboardingLeague(
                league = leagues[0], // Saudi Pro League
                primaryColor = Color(0xFF0A6E3F),
                description = "Saudi Arabia's top-flight professional league"
            ),
            OnboardingLeague(
                league = leagues[1], // Premier League
                primaryColor = Color(0xFF3D195B),
                description = "The most competitive league in the world"
            ),
            OnboardingLeague(
                league = leagues[2], // La Liga
                primaryColor = Color(0xFFFF6B00),
                description = "Home to the world's greatest talents"
            ),
            OnboardingLeague(
                league = leagues[3], // Bundesliga
                primaryColor = Color(0xFFD20515),
                description = "Known for passionate fans and attacking football"
            ),
            OnboardingLeague(
                league = leagues[4], // Serie A
                primaryColor = Color(0xFF004E9F),
                description = "Tactical excellence and rich history"
            ),
            OnboardingLeague(
                league = leagues[5], // Ligue 1
                primaryColor = Color(0xFF1E3A8A),
                description = "Emerging talents and exciting gameplay"
            )
        )
    }

    fun getLeagueById(id: Int): League? = getPopularLeagues().find { it.id == id }

    fun getOnboardingLeagueById(id: Int): OnboardingLeague? =
        getOnboardingLeagues().find { it.league.id == id }
}
