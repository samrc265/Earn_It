package com.example.earnit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.earnit.ui.screens.QuestScreen
import com.example.earnit.ui.screens.RewardsScreen
import com.example.earnit.ui.screens.SettingsScreen
import com.example.earnit.ui.theme.EarnItTheme
import com.example.earnit.viewmodel.MainViewModel

// Define Screens routes
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Quests : Screen("quests", "Quests", Icons.Default.List)
    data object Rewards : Screen("rewards", "Rewards", Icons.Default.Star)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    // We use the Factory we created in the ViewModel to inject the database
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EarnItTheme {
                EarnItApp(viewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarnItApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Items for the Bottom Bar
    val bottomNavItems = listOf(Screen.Quests, Screen.Rewards)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Quests.route

    // Dynamic Title based on screen
    val currentTitle = when(currentRoute) {
        Screen.Quests.route -> "Daily Quests"
        Screen.Rewards.route -> "Rewards & Score"
        Screen.Settings.route -> "Settings"
        else -> "Earn It"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentTitle) },
                actions = {
                    // Info Button
                    IconButton(onClick = {
                        val msg = when(currentRoute) {
                            Screen.Quests.route -> "Complete tasks to earn points!"
                            Screen.Rewards.route -> "Every 100 points = 1 Reward Point"
                            else -> "Earn It App v1.0"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Default.Info, contentDescription = "Info") }

                    // Settings Button
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            // Hide bottom bar on Settings screen
            if (currentRoute != Screen.Settings.route) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Quests.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Quests.route) { QuestScreen(viewModel = viewModel) }
            composable(Screen.Rewards.route) { RewardsScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}