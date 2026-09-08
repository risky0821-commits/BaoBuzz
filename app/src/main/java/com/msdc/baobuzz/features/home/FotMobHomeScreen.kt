package com.msdc.baobuzz.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.msdc.baobuzz.core.models.LiveMatch
import com.msdc.baobuzz.core.models.RecentResult
import com.msdc.baobuzz.core.models.UpcomingFixture
import com.msdc.baobuzz.models.Team

private val AppBlack = Color(0xFF050505)
private val TopBarBlack = Color(0xFF171717)
private val LeagueHeader = Color(0xFF2A2A2A)
private val LeagueBody = Color(0xFF1D1D1D)
private val AccentGreen = Color(0xFF66E47B)
private val Muted = Color(0xFFA9A9A9)
private const val AL_AHLI_ID = 2929

@Composable
fun FotMobHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBlack)
    ) {
        FotMobTopBar(onSettings = onNavigateToSettings)
        DateStrip()

        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingState()
            is HomeUiState.OnboardingRequired -> SimpleMessage("اختر بطولاتك للبدء", "فتح الإعدادات", onNavigateToOnboarding)
            is HomeUiState.NoLeaguesSelected -> SimpleMessage("لا توجد بطولات مختارة", "الإعدادات", onNavigateToSettings)
            is HomeUiState.Error -> SimpleMessage(state.message, "إعادة المحاولة") { viewModel.retry() }
            is HomeUiState.Success -> MatchFeed(state)
        }
    }
}

@Composable
private fun FotMobTopBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarBlack)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettings) {
            Icon(Icons.Default.MoreVert, contentDescription = "المزيد", tint = Color.White)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color.White)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "التقويم", tint = Color.White)
        }

        Spacer(Modifier.weight(1f))

        Surface(
            color = Color(0xFF3A3A3A),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("مباشر", color = Color.White, fontWeight = FontWeight.SemiBold)
                Box(Modifier.size(17.dp).background(Color(0xFF8C8C8C), CircleShape))
            }
        }

        Spacer(Modifier.width(18.dp))

        Text(
            text = "SCORES",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun DateStrip() {
    val days = listOf(
        "الأحد 06 سبتمبر",
        "أمس",
        "اليوم",
        "غداً",
        "الخميس 10 سبتمبر"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarBlack),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        items(days) { label ->
            val selected = label == "اليوم"
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    color = if (selected) Color.White else Muted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .width(50.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) AccentGreen else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AccentGreen)
    }
}

@Composable
private fun MatchFeed(state: HomeUiState.Success) {
    val favoriteUpcoming = state.upcomingFixtures.firstOrNull { it.homeTeam.id == AL_AHLI_ID || it.awayTeam.id == AL_AHLI_ID }
    val favoriteLive = state.liveMatches.firstOrNull { it.homeTeam.id == AL_AHLI_ID || it.awayTeam.id == AL_AHLI_ID }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (favoriteLive != null || favoriteUpcoming != null) {
            item {
                FollowCard(live = favoriteLive, upcoming = favoriteUpcoming)
            }
        }

        if (state.liveMatches.isNotEmpty()) {
            val liveGroups = state.liveMatches.groupBy { it.leagueId }
            items(liveGroups.entries.toList(), key = { "live-${it.key}" }) { entry ->
                CompetitionCard(
                    title = "مباشر",
                    liveMatches = entry.value
                )
            }
        }

        val upcomingGroups = state.upcomingFixtures.groupBy { it.leagueName }
        items(upcomingGroups.entries.toList(), key = { "up-${it.key}" }) { entry ->
            CompetitionCard(
                title = entry.key,
                upcoming = entry.value
            )
        }

        if (upcomingGroups.isEmpty() && state.recentResults.isNotEmpty()) {
            val resultGroups = state.recentResults.groupBy { it.leagueName }
            items(resultGroups.entries.toList(), key = { "res-${it.key}" }) { entry ->
                CompetitionCard(
                    title = entry.key,
                    results = entry.value
                )
            }
        }
    }
}

@Composable
private fun FollowCard(live: LiveMatch?, upcoming: UpcomingFixture?) {
    val home = live?.homeTeam ?: upcoming!!.homeTeam
    val away = live?.awayTeam ?: upcoming!!.awayTeam
    val middle = if (live != null) {
        live.minute?.let { "$it'" } ?: live.status
    } else {
        formatTime(upcoming!!.dateTime)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LeagueBody)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LeagueHeader)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ExpandLess, null, tint = Color.White)
                Spacer(Modifier.weight(1f))
                Text("أتابع", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Default.Star, null, tint = Color.White)
            }
            MatchLine(home, away, middle, live?.homeScore, live?.awayScore, true)
        }
    }
}

@Composable
private fun CompetitionCard(
    title: String,
    liveMatches: List<LiveMatch> = emptyList(),
    upcoming: List<UpcomingFixture> = emptyList(),
    results: List<RecentResult> = emptyList()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LeagueBody)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LeagueHeader)
                    .padding(horizontal = 20.dp, vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ExpandLess, null, tint = Color.White)
                Spacer(Modifier.weight(1f))
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            liveMatches.forEach { match ->
                MatchLine(
                    home = match.homeTeam,
                    away = match.awayTeam,
                    middle = match.minute?.let { "$it'" } ?: match.status,
                    homeScore = match.homeScore,
                    awayScore = match.awayScore,
                    showNotification = match.homeTeam.id == AL_AHLI_ID || match.awayTeam.id == AL_AHLI_ID
                )
            }

            upcoming.forEach { fixture ->
                MatchLine(
                    home = fixture.homeTeam,
                    away = fixture.awayTeam,
                    middle = formatTime(fixture.dateTime),
                    showNotification = fixture.homeTeam.id == AL_AHLI_ID || fixture.awayTeam.id == AL_AHLI_ID
                )
            }

            results.forEach { result ->
                MatchLine(
                    home = result.homeTeam,
                    away = result.awayTeam,
                    middle = "النهاية",
                    homeScore = result.homeScore,
                    awayScore = result.awayScore
                )
            }
        }
    }
}

@Composable
private fun MatchLine(
    home: Team,
    away: Team,
    middle: String,
    homeScore: Int? = null,
    awayScore: Int? = null,
    showNotification: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 18.dp, vertical = 19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showNotification) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF707070),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
        } else {
            Spacer(Modifier.width(24.dp))
        }

        TeamMini(home, Modifier.weight(1f))

        Column(
            modifier = Modifier.width(76.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (homeScore != null || awayScore != null) {
                Text(
                    text = "${homeScore ?: 0} - ${awayScore ?: 0}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = middle,
                    color = Muted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        TeamMini(away, Modifier.weight(1f), reverse = true)
    }
}

@Composable
private fun TeamMini(team: Team, modifier: Modifier = Modifier, reverse: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (reverse) Arrangement.End else Arrangement.Start
    ) {
        if (!reverse) {
            Text(
                team.name,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
        }

        AsyncImage(
            model = team.logo,
            contentDescription = team.name,
            modifier = Modifier.size(34.dp)
        )

        if (reverse) {
            Spacer(Modifier.width(8.dp))
            Text(
                team.name,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

private fun formatTime(raw: String): String {
    val match = Regex("(\\d{2}):(\\d{2})").find(raw)
    return match?.value ?: raw.take(5)
}

@Composable
private fun SimpleMessage(text: String, actionText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.clickable(onClick = onAction),
            color = LeagueHeader,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(actionText, color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
        }
    }
}
