package com.example.earnit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.earnit.ui.screens.LogScreen
import com.example.earnit.ui.screens.QuestScreen
import com.example.earnit.ui.screens.RewardsScreen
import com.example.earnit.ui.screens.SettingsScreen
import com.example.earnit.ui.theme.EarnItTheme
import com.example.earnit.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Quests : Screen("quests", "Quests", Icons.Default.List)
    data object Rewards : Screen("rewards", "Points", Icons.Default.Star)
    data object Log : Screen("log", "Log", Icons.Default.Edit)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observe theme index here
            val themeIndex by mainViewModel.themeIndex.collectAsStateWithLifecycle()

            EarnItTheme(themeIndex = themeIndex) {
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
    val bottomNavItems = listOf(Screen.Quests, Screen.Rewards, Screen.Log)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Quests.route

    val currentTitle = when(currentRoute) {
        Screen.Quests.route -> "Daily Quests"
        Screen.Rewards.route -> "My Points"
        Screen.Log.route -> "Activity Log"
        Screen.Settings.route -> "Settings"
        else -> "Earn It"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    if (currentRoute == Screen.Settings.route) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute != Screen.Settings.route) {
                        IconButton(onClick = {
                            val msg = when(currentRoute) {
                                Screen.Quests.route -> "Tasks disappear when checked!"
                                Screen.Rewards.route -> "Track your score here"
                                Screen.Log.route -> "Keep notes of your rewards"
                                else -> "Earn It App"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.Info, contentDescription = "Info") }

                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
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
            composable(Screen.Log.route) { LogScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel = viewModel) }
        }
    }
}