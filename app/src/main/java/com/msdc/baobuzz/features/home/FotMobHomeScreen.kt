package com.msdc.baobuzz.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.msdc.baobuzz.core.models.LiveMatch
import com.msdc.baobuzz.core.models.RecentResult
import com.msdc.baobuzz.core.models.UpcomingFixture

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        FotMobTopBar(
            onRefresh = viewModel::refreshData,
            onSettings = onNavigateToSettings
        )
        DateStrip()

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.OnboardingRequired -> {
                SimpleMessage("اختر دورياتك للبدء", "فتح الإعدادات", onNavigateToOnboarding)
            }
            is HomeUiState.NoLeaguesSelected -> {
                SimpleMessage("لا توجد دوريات مختارة", "الإعدادات", onNavigateToSettings)
            }
            is HomeUiState.Error -> {
                SimpleMessage(state.message, "إعادة المحاولة") { viewModel.retry() }
            }
            is HomeUiState.Success -> MatchFeed(state)
        }
    }
}

@Composable
private fun FotMobTopBar(onRefresh: () -> Unit, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "المباريات",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "تحديث")
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Default.Settings, contentDescription = "الإعدادات")
        }
    }
}

@Composable
private fun DateStrip() {
    val days = listOf("السبت" to "5", "الأحد" to "6", "الإثنين" to "7", "اليوم" to "8", "الأربعاء" to "9", "الخميس" to "10", "الجمعة" to "11")
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { (name, number) ->
            val selected = name == "اليوم"
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(name, style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(number, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun MatchFeed(state: HomeUiState.Success) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.liveMatches.isNotEmpty()) {
            item { SectionTitle("مباشر الآن") }
            items(state.liveMatches, key = { it.id }) { match -> LiveMatchRow(match) }
        }

        if (state.upcomingFixtures.isNotEmpty()) {
            item { SectionTitle("المباريات القادمة") }
            items(state.upcomingFixtures, key = { it.id }) { fixture -> UpcomingMatchRow(fixture) }
        }

        if (state.recentResults.isNotEmpty()) {
            item { SectionTitle("النتائج") }
            items(state.recentResults, key = { it.id }) { result -> ResultRow(result) }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun MatchCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) { content() }
    }
}

@Composable
private fun LiveMatchRow(match: LiveMatch) = MatchCard {
    TeamRow(match.homeTeam.name, match.homeTeam.logo, match.homeScore?.toString() ?: "-")
    Spacer(Modifier.height(8.dp))
    TeamRow(match.awayTeam.name, match.awayTeam.logo, match.awayScore?.toString() ?: "-")
    Spacer(Modifier.height(8.dp))
    Text(
        text = match.minute?.let { "$it'" } ?: match.status,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun UpcomingMatchRow(fixture: UpcomingFixture) = MatchCard {
    Text(fixture.leagueName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    TeamRow(fixture.homeTeam.name, fixture.homeTeam.logo, "")
    Spacer(Modifier.height(8.dp))
    TeamRow(fixture.awayTeam.name, fixture.awayTeam.logo, "")
    Spacer(Modifier.height(8.dp))
    Text(fixture.dateTime, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ResultRow(result: RecentResult) = MatchCard {
    Text(result.leagueName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    TeamRow(result.homeTeam.name, result.homeTeam.logo, result.homeScore.toString())
    Spacer(Modifier.height(8.dp))
    TeamRow(result.awayTeam.name, result.awayTeam.logo, result.awayScore.toString())
}

@Composable
private fun TeamRow(name: String, logo: String, score: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier.size(28.dp).clip(CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        if (score.isNotEmpty()) Text(score, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SimpleMessage(text: String, actionText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text)
        Spacer(Modifier.height(12.dp))
        Card(onClick = onAction) {
            Text(actionText, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        }
    }
}
