package com.mosalab.submission_awal_faa

import MainViewModelFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mosalab.submission_awal_faa.Data.AppDatabase
import kotlinx.coroutines.delay
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
    var isLoading by remember { mutableStateOf(true) }

    // Simulate loading delay
    LaunchedEffect(Unit) {
        delay(2000)
        isLoading = false
    }

    if (isLoading || activeState.loading || nonActiveState.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Home") })
            },
            content = { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header for Upcoming Events
                    item {
                        Text(
                            text = "Upcoming Events",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Displaying Upcoming Events in a LazyRow
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeState.list) { activeEvent ->
                                EventCard(
                                    event = activeEvent,
                                    navController = navController,
                                    isActive = true,
                                    viewModel = viewModel,
                                    coroutineScope = coroutineScope,
                                    context = LocalContext.current
                                )
                            }
                        }
                    }

                    // Header for Finished Events
                    item {
                        Text(
                            text = "Finished Events",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Displaying Finished Events in LazyColumn
                    items(nonActiveState.list) { event ->
                        EventCard(
                            event = event,
                            navController = navController,
                            isActive = false,
                            viewModel = viewModel,
                            coroutineScope = coroutineScope,
                            context = LocalContext.current
                        )
                    }
                }
            }
        )
    }
}
