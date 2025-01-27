package com.mosalab.submission_awal_faa.Screen

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
import com.mosalab.submission_awal_faa.core.Data.AppDatabase
import com.mosalab.submission_awal_faa.MainViewModel
import com.mosalab.submission_awal_faa.PreferencesManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            com.mosalab.submission_awal_faa.core.Data.AppDatabase.getDatabase(LocalContext.current),
            PreferencesManager(LocalContext.current)
        )
    )
) {
    val nonActiveState by viewModel.eventNonActiveState
    val activeState by viewModel.eventActiveState
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

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
                    item {
                        Text(
                            text = "Upcoming Events",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

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

                    item {
                        Text(
                            text = "Finished Events",
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
                            coroutineScope = coroutineScope,
                            context = LocalContext.current
                        )
                    }
                }
            }
        )
    }
}
