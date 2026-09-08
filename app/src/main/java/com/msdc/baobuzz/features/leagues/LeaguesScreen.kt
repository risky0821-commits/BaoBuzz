package com.msdc.baobuzz.features.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.msdc.baobuzz.core.data.LeagueData
import com.msdc.baobuzz.models.League

@Composable
fun LeaguesScreen(
    navController: NavHostController,
    viewModel: LeaguesViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val selectedIds by viewModel.selectedLeagueIds.collectAsState()
    val leagues = remember { LeagueData.getPopularLeagues() }
    val filtered = leagues.filter {
        query.isBlank() ||
            arabicLeagueName(it.name).contains(query.trim(), ignoreCase = true) ||
            it.name.contains(query.trim(), ignoreCase = true)
    }

    val background = Color(0xFF0D111B)
    val panel = Color(0xFF1B2031)
    val accent = Color(0xFF36D8C2)
    val muted = Color(0xFF7A839B)
    val gold = Color(0xFFFFC107)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "البطولات",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${selectedIds.size} مختارة",
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("ابحث عن بطولة", color = muted) },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = muted)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = panel,
                unfocusedContainerColor = panel,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = accent
            ),
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = "اختر فقط البطولات التي تريد ظهور مبارياتها في الرئيسية",
            color = muted,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { league ->
                LeagueRow(
                    league = league,
                    selected = league.id in selectedIds,
                    onToggle = { viewModel.toggleLeague(league.id) },
                    panelColor = panel,
                    mutedColor = muted,
                    goldColor = gold
                )
            }
        }
    }
}

@Composable
private fun LeagueRow(
    league: League,
    selected: Boolean,
    onToggle: () -> Unit,
    panelColor: Color,
    mutedColor: Color,
    goldColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        color = panelColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = league.logo,
                contentDescription = league.name,
                modifier = Modifier.size(38.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = arabicLeagueName(league.name),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = league.name,
                    color = mutedColor,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (selected) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (selected) "إزالة" else "إضافة",
                    tint = if (selected) goldColor else mutedColor
                )
            }
        }
    }
}

private fun arabicLeagueName(name: String): String = when (name.lowercase()) {
    "saudi pro league" -> "دوري روشن السعودي"
    "uefa champions league" -> "دوري أبطال أوروبا"
    "premier league" -> "الدوري الإنجليزي"
    "la liga" -> "الدوري الإسباني"
    "bundesliga" -> "الدوري الألماني"
    "serie a" -> "الدوري الإيطالي"
    "ligue 1" -> "الدوري الفرنسي"
    else -> name
}
