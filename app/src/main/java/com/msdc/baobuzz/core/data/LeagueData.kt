package com.msdc.baobuzz.core.data

import androidx.compose.ui.graphics.Color
import com.msdc.baobuzz.models.League
import java.time.LocalDate

object LeagueData {

    private fun currentSeasonStartYear(today: LocalDate = LocalDate.now()): Int =
        if (today.monthValue >= 7) today.year else today.year - 1

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
                id = 2,
                name = "UEFA Champions League",
                type = "Cup",
                country = "World",
                logo = "https://media.api-sports.io/football/leagues/2.png",
                flag = null,
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

    data class OnboardingLeague(
        val league: League,
        val primaryColor: Color,
        val description: String,
        val isPopular: Boolean = true
    )

    fun getOnboardingLeagues(): List<OnboardingLeague> =
        getPopularLeagues().map { league ->
            val color = when (league.id) {
                307 -> Color(0xFF0A6E3F)
                2 -> Color(0xFF123A7A)
                39 -> Color(0xFF3D195B)
                140 -> Color(0xFFFF6B00)
                78 -> Color(0xFFD20515)
                135 -> Color(0xFF004E9F)
                else -> Color(0xFF1E3A8A)
            }
            OnboardingLeague(
                league = league,
                primaryColor = color,
                description = league.name
            )
        }

    fun getLeagueById(id: Int): League? = getPopularLeagues().find { it.id == id }

    fun getOnboardingLeagueById(id: Int): OnboardingLeague? =
        getOnboardingLeagues().find { it.league.id == id }
}
