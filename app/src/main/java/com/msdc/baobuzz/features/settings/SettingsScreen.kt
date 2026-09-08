package com.msdc.baobuzz.features.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.msdc.baobuzz.models.League

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "الإعدادات",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        when (val current = state) {
            is SettingsUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("جاري تحميل الإعدادات...", color = Color.White)
                }
            }

            is SettingsUiState.Error -> {
                SimpleCard {
                    Text("تعذر تحميل الإعدادات", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(current.message, color = Color.LightGray)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::retry) { Text("إعادة المحاولة") }
                }
            }

            is SettingsUiState.DataCleared -> {
                SimpleCard {
                    Text("تمت إعادة ضبط الإعدادات", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onNavigateToOnboarding) { Text("متابعة") }
                }
            }

            is SettingsUiState.Loaded -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        SettingsSection(title = "البطولات", icon = Icons.Default.SportsSoccer) {
                            Text(
                                "المختارة (${current.selectedLeagues.size})",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            if (current.selectedLeagues.isEmpty()) {
                                Text("لم تختر أي بطولة", color = Color.LightGray)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(current.selectedLeagues, key = { it.id }) { league ->
                                        SelectedLeagueChip(
                                            league = league,
                                            onRemove = { viewModel.removeLeague(league) }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Text("إضافة بطولة", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            val remaining = viewModel.availableLeagues.filter { available ->
                                current.selectedLeagues.none { it.id == available.id }
                            }
                            if (remaining.isEmpty()) {
                                Text("كل البطولات المتاحة مختارة", color = Color.LightGray)
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(remaining, key = { it.id }) { league ->
                                        AvailableLeagueChip(
                                            league = league,
                                            onAdd = { viewModel.addLeague(league) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SettingsSection(title = "التنبيهات", icon = Icons.Default.Notifications) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("تنبيهات المباريات", color = Color.White)
                                    Text(
                                        "التنبيه عند أحداث المباريات المتابَعة",
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = current.notificationsEnabled,
                                    onCheckedChange = viewModel::toggleNotifications
                                )
                            }
                        }
                    }

                    item {
                        SettingsSection(title = "اللغة") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("لغة التطبيق", color = Color.White)
                                    Text(
                                        SupportedLanguage.entries
                                            .firstOrNull { it.code == current.preferredLanguage }
                                            ?.displayName ?: "العربية",
                                        color = Color.LightGray
                                    )
                                }
                                TextButton(onClick = { showLanguageDialog = true }) {
                                    Text("تغيير")
                                }
                            }
                        }
                    }

                    item {
                        SettingsSection(title = "البيانات") {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showClearDialog = true }
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("مسح الإعدادات المحفوظة")
                            }
                        }
                    }

                    item {
                        SettingsSection(title = "حول التطبيق") {
                            Text("Sama Scores", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("بيانات المباريات: API-Football", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("اختر اللغة") },
            text = {
                Column {
                    SupportedLanguage.entries.forEach { language ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (state as? SettingsUiState.Loaded)?.preferredLanguage == language.code,
                                onClick = {
                                    viewModel.changeLanguage(language.code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(language.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("إغلاق") }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("مسح الإعدادات؟") },
            text = { Text("سيتم حذف اختيارات البطولات والمتابعات المحفوظة من الجهاز.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) { Text("مسح") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SimpleCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SelectedLeagueChip(league: League, onRemove: () -> Unit) {
    AssistChip(
        onClick = onRemove,
        label = { Text(arabicLeagueName(league.name)) },
        leadingIcon = {
            AsyncImage(model = league.logo, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailableLeagueChip(league: League, onAdd: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onAdd,
        label = { Text(arabicLeagueName(league.name)) },
        leadingIcon = {
            AsyncImage(model = league.logo, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    )
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
