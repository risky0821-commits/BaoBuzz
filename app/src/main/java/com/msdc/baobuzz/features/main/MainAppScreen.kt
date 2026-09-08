package com.msdc.baobuzz.features.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.msdc.baobuzz.core.navigation.BaoBuzzRoutes
import com.msdc.baobuzz.features.home.FotMobHomeScreen
import com.msdc.baobuzz.features.leagues.LeaguesScreen
import com.msdc.baobuzz.features.settings.SettingsScreen
import com.msdc.baobuzz.presentation.transfers.TransfersScreen

@Composable
fun MainAppScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToFacts: () -> Unit = {},
    onNavigateToComparison: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(BaoBuzzRoutes.HOME, Icons.Default.SportsSoccer, "المباريات"),
        BottomNavItem(BaoBuzzRoutes.NEWS, Icons.Default.Article, "الأخبار"),
        BottomNavItem(BaoBuzzRoutes.LEAGUES, Icons.Default.EmojiEvents, "البطولات"),
        BottomNavItem(BaoBuzzRoutes.FAVORITES, Icons.Default.Star, "أتابع"),
        BottomNavItem(BaoBuzzRoutes.SETTINGS, Icons.Default.Menu, "المزيد")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(BaoBuzzRoutes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BaoBuzzRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BaoBuzzRoutes.HOME) {
                FotMobHomeScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(BaoBuzzRoutes.ONBOARDING) {
                            popUpTo(BaoBuzzRoutes.HOME) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate(BaoBuzzRoutes.SETTINGS) }
                )
            }

            composable(BaoBuzzRoutes.NEWS) {
                PlaceholderScreen("الأخبار", "واجهة الأخبار ستُربط بمصدر الأخبار لاحقًا")
            }
            composable(BaoBuzzRoutes.LEAGUES) { LeaguesScreen(navController = navController) }
            composable(BaoBuzzRoutes.FAVORITES) {
                PlaceholderScreen("أتابع", "هنا ستظهر الفرق والبطولات التي تتابعها")
            }
            composable(BaoBuzzRoutes.TRANSFERS) {
                val teamId = it.arguments?.getString("teamId")?.toIntOrNull()
                if (teamId != null) TransfersScreen(teamId = teamId, navController = navController)
            }
            composable(BaoBuzzRoutes.SETTINGS) {
                SettingsScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(BaoBuzzRoutes.ONBOARDING) {
                            popUpTo(BaoBuzzRoutes.HOME) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "قيد التجهيز",
                modifier = Modifier.padding(20.dp).align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)
