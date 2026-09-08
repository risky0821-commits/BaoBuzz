package com.msdc.baobuzz.features.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.navigation.NavHostController

private data class PersonalCompetition(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val favoriteByDefault: Boolean = true
)

private val personalCompetitions = listOf(
    PersonalCompetition("saudi-pro-league", "دوري روشن السعودي", "Saudi Pro League"),
    PersonalCompetition("afc-champions-league-elite", "دوري أبطال آسيا للنخبة", "AFC Champions League Elite"),
    PersonalCompetition("saudi-kings-cup", "كأس خادم الحرمين الشريفين", "King's Cup"),
    PersonalCompetition("uefa-champions-league", "دوري أبطال أوروبا", "UEFA Champions League"),
    PersonalCompetition("premier-league", "الدوري الإنجليزي", "Premier League"),
    PersonalCompetition("spanish-super-cup", "كأس السوبر الإسباني", "Spanish Super Cup"),
    PersonalCompetition("la-liga", "الدوري الإسباني", "La Liga"),
    PersonalCompetition("serie-a", "الدوري الإيطالي", "Serie A"),
    PersonalCompetition("intercontinental-cup", "كأس إنتركونتيننتال", "FIFA Intercontinental Cup"),
    PersonalCompetition("europa-league", "الدوري الأوروبي", "UEFA Europa League"),
    PersonalCompetition("conference-league", "دوري المؤتمر الأوروبي", "UEFA Conference League"),
    PersonalCompetition("caf-champions-league", "دوري أبطال أفريقيا", "CAF Champions League"),
    PersonalCompetition("caf-confederation-cup", "كأس الكونفدرالية", "CAF Confederation Cup"),
    PersonalCompetition("afc-champions-league-two", "دوري أبطال آسيا 2", "AFC Champions League Two"),
    PersonalCompetition("gulf-club-champions-league", "دوري أبطال الخليج", "Gulf Club Champions League"),
    PersonalCompetition("fa-cup", "كأس الاتحاد الإنجليزي", "FA Cup"),
    PersonalCompetition("efl-cup", "كأس الرابطة الإنجليزية", "EFL Cup"),
    PersonalCompetition("coppa-italia", "كأس إيطاليا", "Coppa Italia"),
    PersonalCompetition("fifa-world-cup", "كأس العالم", "FIFA World Cup"),
    PersonalCompetition("uefa-nations-league", "دوري الأمم الأوروبية", "UEFA Nations League"),
    PersonalCompetition("afc-asian-cup", "كأس آسيا", "AFC Asian Cup"),
    PersonalCompetition("gulf-cup-27", "خليجي 27", "Gulf Cup 27")
)

@Composable
fun LeaguesScreen(
    navController: NavHostController
) {
    var query by remember { mutableStateOf("") }
    val favorites = remember {
        mutableStateListOf<String>().apply {
            addAll(personalCompetitions.filter { it.favoriteByDefault }.map { it.id })
        }
    }

    val filtered = remember(query) {
        if (query.isBlank()) {
            personalCompetitions
        } else {
            val normalized = query.trim().lowercase()
            personalCompetitions.filter {
                it.arabicName.contains(query.trim(), ignoreCase = true) ||
                    it.englishName.lowercase().contains(normalized)
            }
        }
    }

    val background = Color(0xFF0D111B)
    val panel = Color(0xFF1B2031)
    val panelAlt = Color(0xFF202638)
    val accent = Color(0xFF36D8C2)
    val muted = Color(0xFF7A839B)
    val gold = Color(0xFFFFC107)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

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
                text = "22",
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
            singleLine = true,
            placeholder = {
                Text("ابحث عن بطولة", color = muted)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = muted
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = panel,
                unfocusedContainerColor = panel,
                disabledContainerColor = panel,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = accent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = panel,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = accent,
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المفضلة  ${favorites.size}",
                            color = Color(0xFF0D111B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = panel,
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text(
                        text = "البطولات المحددة  22",
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = muted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = filtered,
                key = { _, item -> item.id }
            ) { _, competition ->
                CompetitionRow(
                    competition = competition,
                    isFavorite = competition.id in favorites,
                    panelColor = panelAlt,
                    mutedColor = muted,
                    goldColor = gold,
                    onToggleFavorite = {
                        if (competition.id in favorites) {
                            favorites.remove(competition.id)
                        } else {
                            favorites.add(competition.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CompetitionRow(
    competition: PersonalCompetition,
    isFavorite: Boolean,
    panelColor: Color,
    mutedColor: Color,
    goldColor: Color,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        color = panelColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = null,
                tint = mutedColor.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111827)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = competition.arabicName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = competition.arabicName,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = competition.englishName,
                    color = mutedColor,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (isFavorite) goldColor else mutedColor
                )
            }
        }
    }
}
