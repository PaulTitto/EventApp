package com.mosalab.submission_awal_faa

import MainViewModelFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mosalab.submission_awal_faa.Data.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            AppDatabase.getDatabase(LocalContext.current),
            PreferencesManager(LocalContext.current)
        )
    )
) {
    val nonActiveState by viewModel.eventNonActiveState
    val activeState by viewModel.eventActiveState
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Upcoming Events",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                items(activeState.list) { activeEvent ->
                    EventCard(
                        event = activeEvent,
                        navController = navController,
                        isActive = true,
                        viewModel = viewModel,
                        onFavoriteClick = {
                            coroutineScope.launch {
                                if (viewModel.isFavorite(activeEvent.id)) {
                                    viewModel.removeFavorite(activeEvent.toFavoriteEvent())
                                } else {
                                    viewModel.addFavorite(activeEvent.toFavoriteEvent())
                                }
                            }
                        }
                    )
                }

                item {
                    Text(
                        "Finished Events",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                items(nonActiveState.list) { event ->
                    EventCard(
                        event = event,
                        navController = navController,
                        isActive = false,
                        viewModel = viewModel,
                        onFavoriteClick = {
                            coroutineScope.launch {
                                if (viewModel.isFavorite(event.id)) {
                                    viewModel.removeFavorite(event.toFavoriteEvent())
                                } else {
                                    viewModel.addFavorite(event.toFavoriteEvent())
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}
