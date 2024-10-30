package com.mosalab.submission_awal_faa

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mosalab.submission_awal_faa.Data.DetailEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailEventActiveScreen(viewModel: MainViewModel, navController: NavController) {
    val state by viewModel.eventActiveState
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upcoming") })
        }
    ) { paddingValues ->
        LoadingContent(state.loading, state.error) {
            EventList(
                events = state.list,
                navController = navController,
                viewModel = viewModel,
                isActive = true,
                paddingValues = paddingValues,
                context = context,
                coroutineScope = coroutineScope
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailEventNonActiveScreen(viewModel: MainViewModel, navController: NavController) {
    val state by viewModel.eventNonActiveState
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Finished") })
        }
    ) { paddingValues ->
        LoadingContent(state.loading, state.error) {
            EventList(
                events = state.list,
                navController = navController,
                viewModel = viewModel,
                isActive = false,
                paddingValues = paddingValues,
                context = context,
                coroutineScope = coroutineScope
            )
        }
    }
}

@Composable
fun EventList(
    events: List<DetailEvent>,
    navController: NavController,
    viewModel: MainViewModel,
    isActive: Boolean,
    paddingValues: PaddingValues,
    context: android.content.Context,
    coroutineScope: CoroutineScope
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(paddingValues)
    ) {
        items(events) { event ->
            EventCard(
                event = event,
                navController = navController,
                isActive = isActive,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                context = context
            )
        }
    }
}

@Composable
fun EventCard(
    event: DetailEvent,
    navController: NavController,
    isActive: Boolean,
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope,
    context: android.content.Context
) {
    var isFavorite by remember { mutableStateOf(false) }

    // Load the favorite state in the background
    LaunchedEffect(event.id) {
        isFavorite = withContext(Dispatchers.IO) {
            viewModel.isFavorite(event.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(100.dp)
            .clickable {
                val route = if (isActive) "de_active/${event.id}" else "finished_events/${event.id}"
                navController.navigate(route)
            }
    ) {
        EventCardContent(
            event = event,
            isFavorite = isFavorite,
            onFavoriteClick = {
                coroutineScope.launch {
                    if (isFavorite) {
                        viewModel.removeFavorite(event.toFavoriteEvent())
                        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addFavorite(event.toFavoriteEvent())
                        Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                    isFavorite = !isFavorite // Toggle favorite state
                }
            }
        )
    }
}

@Composable
fun EventCardContent(
    event: DetailEvent,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = event.imageLogo),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(2f)
                .padding(8.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Summary: ${event.summary}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }

        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Toggle Favorite",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun toggleFavorite(
    event: DetailEvent,
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope,
    context: android.content.Context
) {
    coroutineScope.launch {
        val isFavorite = viewModel.isFavorite(event.id)
        if (isFavorite) {
            viewModel.removeFavorite(event.toFavoriteEvent())
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addFavorite(event.toFavoriteEvent())
            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
        }
    }
}
