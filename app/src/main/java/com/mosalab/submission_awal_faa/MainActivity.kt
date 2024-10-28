package com.mosalab.submission_awal_faa

import FavoriteEventsScreen
import MainViewModelFactory
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mosalab.submission_awal_faa.Data.AppDatabase
import com.mosalab.submission_awal_faa.Screen.SettingsScreen
import com.mosalab.submission_awal_faa.ui.theme.SubmissionawalfaaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val database = AppDatabase.getDatabase(applicationContext)
            val preferencesManager = PreferencesManager(applicationContext)

            val mainViewModel: MainViewModel = ViewModelProvider(
                this,
                MainViewModelFactory(database, preferencesManager)
            ).get(MainViewModel::class.java)

            val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)

            SubmissionawalfaaTheme(darkTheme = isDarkMode) {
                MyApp(mainViewModel)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentRoute = getCurrentRoute(navController)
    var selectedTab by remember { mutableStateOf(0) }

    val titles = mapOf(
        "home" to "Home",
        "de_active" to "Upcoming Events",
        "finished_events" to "Finished Events",
        "favorites" to "Favorites",
        "settings" to "Settings"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(titles[currentRoute] ?: "App Title") })
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    val destination = when (index) {
                        0 -> "home"
                        1 -> "de_active"
                        2 -> "finished_events"
                        3 -> "favorites"
                        4 -> "settings"
                        else -> "home"
                    }
                    navController.navigate(destination) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(viewModel = viewModel, navController = navController)
            }
            composable("de_active") {
                DetailEventActiveScreen(viewModel = viewModel, navController)
            }
            composable("de_active/{id}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("id") ?: ""
                DetailEventScreen(
                    navController = navController,
                    eventId = eventId,
                    viewModel = viewModel,
                    isActive = true
                )
            }
            composable("finished_events") {
                DetailEventNonActiveScreen(viewModel = viewModel, navController)
            }
            composable("finished_events/{id}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("id") ?: ""
                DetailEventScreen(
                    navController = navController,
                    eventId = eventId,
                    viewModel = viewModel,
                    isActive = false
                )
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel, navController = navController)
            }
            composable("favorites") {
                FavoriteEventsScreen(viewModel = viewModel, navController = navController)
            }
        }
    }
}


@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (index: Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("Home",fontSize = 10.sp) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Upcoming",fontSize = 10.sp) },
            icon = { Icon(Icons.Default.List, contentDescription = "Upcoming") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Finished",fontSize = 10.sp) },
            icon = { Icon(Icons.Default.Check, contentDescription = "Finished") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            label = { Text("Favorites",fontSize = 10.sp) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") }
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            label = { Text("Settings",fontSize = 10.sp) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
        )
    }
}

@Composable
fun getCurrentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
