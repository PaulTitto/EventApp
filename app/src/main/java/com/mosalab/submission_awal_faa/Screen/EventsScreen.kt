package com.mosalab.submission_awal_faa.Screen

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.mosalab.submission_awal_faa.core.Data.DetailEvent
import com.mosalab.submission_awal_faa.MainViewModel
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
    events: List<com.mosalab.submission_awal_faa.core.Data.DetailEvent>,
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
    event: com.mosalab.submission_awal_faa.core.Data.DetailEvent,
    navController: NavController,
    isActive: Boolean,
    viewModel: MainViewModel,
    coroutineScope: CoroutineScope,
    context: android.content.Context
) {
    var isFavorite by remember { mutableStateOf(false) }

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
                    isFavorite = !isFavorite
                }
            }
        )
    }
}


@Composable
fun EventCardContent(
    event: com.mosalab.submission_awal_faa.core.Data.DetailEvent,
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
        Box(modifier = Modifier
            .weight(1f)
            .height(100.dp)) {

            val painter = rememberAsyncImagePainter(
                model = event.imageLogo,
            )

            if (painter.state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }


        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(2f)
                .padding(8.dp)
        ) {
            Text(
                text = event.name,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = event.summary,
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.W200),
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
    event: com.mosalab.submission_awal_faa.core.Data.DetailEvent,
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
