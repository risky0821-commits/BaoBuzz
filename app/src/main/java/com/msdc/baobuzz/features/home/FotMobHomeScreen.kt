package com.msdc.baobuzz.features.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.msdc.baobuzz.core.data.PersonalFootballDefaults
import com.msdc.baobuzz.core.models.LiveMatch
import com.msdc.baobuzz.core.models.RecentResult
import com.msdc.baobuzz.core.models.UpcomingFixture
import com.msdc.baobuzz.models.Team
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AppBlack = Color(0xFF050505)
private val TopBarBlack = Color(0xFF171717)
private val LeagueHeader = Color(0xFF2A2A2A)
private val LeagueBody = Color(0xFF1D1D1D)
private val AccentGreen = Color(0xFF66E47B)
private val Muted = Color(0xFFA9A9A9)
private val RiyadhZone = ZoneId.of("Asia/Riyadh")
private const val AL_AHLI_ID = PersonalFootballDefaults.AL_AHLI_JEDDAH_TEAM_ID

@Composable
fun FotMobHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val followedMatchIds by viewModel.followedMatchIds.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now(RiyadhZone)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBlack)
    ) {
        ScoresTopBar(onSettings = onNavigateToSettings)
        DateStrip(selectedDate = selectedDate, onSelectDate = { selectedDate = it })

        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingState()
            is HomeUiState.OnboardingRequired -> SimpleMessage("اختر بطولاتك للبدء", "فتح الإعدادات", onNavigateToOnboarding)
            is HomeUiState.NoLeaguesSelected -> SimpleMessage("لا توجد بطولات مختارة", "الإعدادات", onNavigateToSettings)
            is HomeUiState.Error -> SimpleMessage(state.message, "إعادة المحاولة") { viewModel.retry() }
            is HomeUiState.Success -> MatchFeed(
                state = state,
                selectedDate = selectedDate,
                followedMatchIds = followedMatchIds,
                onToggleFollow = viewModel::toggleFollowMatch
            )
        }
    }
}

@Composable
private fun ScoresTopBar(onSettings: () -> Unit) {
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
        Spacer(Modifier.weight(1f))
        Surface(color = Color(0xFF3A3A3A), shape = RoundedCornerShape(24.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("مباشر", color = Color.White, fontWeight = FontWeight.SemiBold)
                Box(Modifier.size(10.dp).background(AccentGreen, CircleShape))
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = "المباريات",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DateStrip(selectedDate: LocalDate, onSelectDate: (LocalDate) -> Unit) {
    val today = LocalDate.now(RiyadhZone)
    val formatter = remember { DateTimeFormatter.ofPattern("EEE dd MMM", Locale("ar")) }
    val days = remember(today) {
        (-2L..2L).map { offset ->
            val date = today.plusDays(offset)
            val label = when (offset) {
                -1L -> "أمس"
                0L -> "اليوم"
                1L -> "غداً"
                else -> date.format(formatter)
            }
            date to label
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().background(TopBarBlack),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        items(days, key = { it.first.toString() }) { (date, label) ->
            val selected = date == selectedDate
            Column(
                modifier = Modifier
                    .clickable { onSelectDate(date) }
                    .padding(vertical = 10.dp),
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
private fun MatchFeed(
    state: HomeUiState.Success,
    selectedDate: LocalDate,
    followedMatchIds: Set<String>,
    onToggleFollow: (String) -> Unit
) {
    val today = LocalDate.now(RiyadhZone)
    val live = if (selectedDate == today) state.liveMatches else emptyList()
    val upcoming = state.upcomingFixtures.filter { matchDate(it.dateTime) == selectedDate }
    val results = state.recentResults.filter { matchDate(it.date) == selectedDate }

    val followedLive = live.filter { it.id in followedMatchIds || isAlAhli(it.homeTeam, it.awayTeam) }
    val followedUpcoming = upcoming.filter { it.id in followedMatchIds || isAlAhli(it.homeTeam, it.awayTeam) }
    val followedResults = results.filter { it.id in followedMatchIds || isAlAhli(it.homeTeam, it.awayTeam) }

    val leagueIds = buildSet {
        live.forEach { add(it.leagueId) }
        upcoming.forEach { add(it.leagueId) }
        results.forEach { add(it.leagueId) }
    }.toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (followedLive.isNotEmpty() || followedUpcoming.isNotEmpty() || followedResults.isNotEmpty()) {
            item(key = "follow-card") {
                CompetitionCard(
                    title = "أتابع",
                    liveMatches = followedLive,
                    upcoming = followedUpcoming,
                    results = followedResults,
                    followedMatchIds = followedMatchIds,
                    onToggleFollow = onToggleFollow,
                    showStar = true
                )
            }
        }

        items(leagueIds, key = { "league-$it" }) { leagueId ->
            CompetitionCard(
                title = leagueTitle(leagueId, state),
                liveMatches = live.filter { it.leagueId == leagueId },
                upcoming = upcoming.filter { it.leagueId == leagueId },
                results = results.filter { it.leagueId == leagueId },
                followedMatchIds = followedMatchIds,
                onToggleFollow = onToggleFollow
            )
        }

        if (leagueIds.isEmpty()) {
            item(key = "empty-day") {
                Text(
                    text = "لا توجد مباريات في هذا اليوم",
                    color = Muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun CompetitionCard(
    title: String,
    liveMatches: List<LiveMatch> = emptyList(),
    upcoming: List<UpcomingFixture> = emptyList(),
    results: List<RecentResult> = emptyList(),
    followedMatchIds: Set<String>,
    onToggleFollow: (String) -> Unit,
    showStar: Boolean = false
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
                if (showStar) {
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.Default.Star, null, tint = AccentGreen)
                }
            }

            liveMatches.forEach { match ->
                MatchLine(
                    matchId = match.id,
                    home = match.homeTeam,
                    away = match.awayTeam,
                    middle = match.minute?.let { "$it'" } ?: match.status,
                    homeScore = match.homeScore,
                    awayScore = match.awayScore,
                    isFollowed = match.id in followedMatchIds || isAlAhli(match.homeTeam, match.awayTeam),
                    onToggleFollow = onToggleFollow
                )
            }

            upcoming.forEach { fixture ->
                MatchLine(
                    matchId = fixture.id,
                    home = fixture.homeTeam,
                    away = fixture.awayTeam,
                    middle = formatRiyadhTime(fixture.dateTime),
                    isFollowed = fixture.id in followedMatchIds || isAlAhli(fixture.homeTeam, fixture.awayTeam),
                    onToggleFollow = onToggleFollow
                )
            }

            results.forEach { result ->
                MatchLine(
                    matchId = result.id,
                    home = result.homeTeam,
                    away = result.awayTeam,
                    middle = "النهاية",
                    homeScore = result.homeScore,
                    awayScore = result.awayScore,
                    isFollowed = result.id in followedMatchIds || isAlAhli(result.homeTeam, result.awayTeam),
                    onToggleFollow = onToggleFollow
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchLine(
    matchId: String,
    home: Team,
    away: Team,
    middle: String,
    homeScore: Int? = null,
    awayScore: Int? = null,
    isFollowed: Boolean,
    onToggleFollow: (String) -> Unit
) {
    var showFollowDialog by remember(matchId) { mutableStateOf(false) }
    val isAlAhliMatch = isAlAhli(home, away)

    if (showFollowDialog) {
        AlertDialog(
            onDismissRequest = { showFollowDialog = false },
            title = { Text(if (isFollowed) "إلغاء متابعة المباراة؟" else "تابع المباراة؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleFollow(matchId)
                        showFollowDialog = false
                    }
                ) {
                    Text(if (isFollowed) "إلغاء المتابعة" else "تابع المباراة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFollowDialog = false }) { Text("إلغاء") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    // Al-Ahli is followed automatically, so no redundant action is shown.
                    if (!isAlAhliMatch) showFollowDialog = true
                }
            )
            .padding(horizontal = 18.dp, vertical = 19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isFollowed) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "متابَع",
                tint = AccentGreen,
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

private fun isAlAhli(home: Team, away: Team): Boolean = home.id == AL_AHLI_ID || away.id == AL_AHLI_ID

private fun leagueTitle(leagueId: Int, state: HomeUiState.Success): String =
    state.selectedLeagues.firstOrNull { it.id == leagueId }?.name?.let(::arabicLeagueName)
        ?: state.upcomingFixtures.firstOrNull { it.leagueId == leagueId }?.leagueName?.let(::arabicLeagueName)
        ?: state.recentResults.firstOrNull { it.leagueId == leagueId }?.leagueName?.let(::arabicLeagueName)
        ?: "المباريات"

private fun arabicLeagueName(name: String): String = when (name.lowercase()) {
    "saudi pro league", "pro league" -> "دوري روشن السعودي"
    "premier league" -> "الدوري الإنجليزي"
    "la liga" -> "الدوري الإسباني"
    "bundesliga" -> "الدوري الألماني"
    "serie a" -> "الدوري الإيطالي"
    "ligue 1" -> "الدوري الفرنسي"
    else -> name
}

private fun matchDate(raw: String): LocalDate? =
    runCatching { OffsetDateTime.parse(raw).atZoneSameInstant(RiyadhZone).toLocalDate() }.getOrElse {
        runCatching { Instant.parse(raw).atZone(RiyadhZone).toLocalDate() }.getOrNull()
    }

private fun formatRiyadhTime(raw: String): String =
    runCatching {
        OffsetDateTime.parse(raw)
            .atZoneSameInstant(RiyadhZone)
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrElse {
        runCatching {
            Instant.parse(raw).atZone(RiyadhZone).format(DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrElse { raw.take(5) }
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
