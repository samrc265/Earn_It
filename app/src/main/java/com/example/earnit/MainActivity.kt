package com.example.earnit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
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
import com.example.earnit.ui.screens.ForestScreen
import com.example.earnit.ui.screens.LogScreen
import com.example.earnit.ui.screens.PlantScreen
import com.example.earnit.ui.screens.QuestScreen
import com.example.earnit.ui.screens.RewardsScreen
import com.example.earnit.ui.screens.SettingsScreen
import com.example.earnit.ui.theme.EarnItTheme
import com.example.earnit.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Quests : Screen("quests", "Quests", Icons.AutoMirrored.Filled.List)
    data object Rewards : Screen("rewards", "Progress", Icons.Default.Star)
    data object Plant : Screen("plant", "My Plant", Icons.Default.Face)
    data object Log : Screen("log", "Reward Log", Icons.Default.Edit)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Forest : Screen("forest", "Forest", Icons.Default.Star)
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeIndex by mainViewModel.themeIndex.collectAsStateWithLifecycle()
            val darkMode by mainViewModel.darkMode.collectAsStateWithLifecycle()

            EarnItTheme(themeIndex = themeIndex, darkModePreference = darkMode) {
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

    // Plant added to the bottom navigation list
    val bottomNavItems = listOf(Screen.Quests, Screen.Rewards, Screen.Plant, Screen.Log)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Quests.route

    var showInfoDialog by remember { mutableStateOf(false) }

    val currentTitle = when(currentRoute) {
        Screen.Quests.route -> "Quests"
        Screen.Rewards.route -> "Progress"
        Screen.Plant.route -> "My Plant"
        Screen.Log.route -> "Reward Log"
        Screen.Settings.route -> "Settings"
        else -> "Earn It"
    }

    val infoMessage = when(currentRoute) {
        Screen.Quests.route -> "Complete quests to earn XP!\n\n• Daily Quests: 2 XP\n• Short Term: 10 XP\n• Long Term: 25 XP\n\nCompleted tasks move to the bottom list."
        Screen.Rewards.route -> "Track your Level and XP.\n\nEvery 100 XP you earn converts into 1 Reward Point.\n\nUse Reward Points to redeem real-life treats!"
        Screen.Plant.route -> "Grow your plant!\n\n• Water it by completing 2/3rds of your Daily Quests.\n• Missing days makes it wilt.\n• 4 days of neglect kills it.\n• Keep a fully grown tree for 3 days to add it to your Forest."
        Screen.Log.route -> "Log your rewards here.\n\nEach entry costs 1 Reward Point.\n\nYou can tap an entry to edit or delete it (deleting refunds the point)."
        else -> "Earn It helps you gamify your life by tracking tasks and earning rewards."
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About $currentTitle") },
            text = { Text(infoMessage) },
            confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Got it") } }
        )
    }

    // Hide Bottom Bar on Forest screen and Settings
    val showBottomBar = currentRoute != Screen.Settings.route && currentRoute != Screen.Forest.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // We hide the main TopBar on the Forest screen because ForestScreen has its own TopBar
            if (currentRoute != Screen.Forest.route) {
                CenterAlignedTopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        if (currentRoute == Screen.Settings.route) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (currentRoute != Screen.Settings.route) {
                            IconButton(onClick = { showInfoDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info")
                            }
                            IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
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
            composable(Screen.Plant.route) {
                PlantScreen(
                    viewModel = viewModel,
                    onNavigateToForest = { navController.navigate(Screen.Forest.route) }
                )
            }
            composable(Screen.Log.route) { LogScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel = viewModel) }
            composable(Screen.Forest.route) {
                ForestScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}