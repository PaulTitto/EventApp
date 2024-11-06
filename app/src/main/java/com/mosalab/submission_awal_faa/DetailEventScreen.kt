@file:Suppress("NAME_SHADOWING")

package com.mosalab.submission_awal_faa

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mosalab.submission_awal_faa.Data.DetailEvent
import com.mosalab.submission_awal_faa.Data.FavoriteEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailEventScreen(
    navController: NavController,
    eventId: String,
    viewModel: MainViewModel,
    isActive: Boolean
) {
    val context = LocalContext.current
    val eventState = if (isActive) viewModel.eventActiveState else viewModel.eventNonActiveState
    val parsedEventId = eventId.toIntOrNull()

    if (parsedEventId == null) {
        ErrorScreen(message = "Invalid event ID")
        return
    }

    // Collect event state changes
    val event = remember { mutableStateOf<DetailEvent?>(null) }
    LaunchedEffect(parsedEventId) {
        event.value = eventState.value.list.firstOrNull { it.id == parsedEventId }
            ?: viewModel.getFavoriteEventById(parsedEventId)
        if (event.value == null) {
            Log.e("DetailEventScreen", "Event with ID $parsedEventId not found")
        }
    }

    // Display loading if event data is not yet loaded
    if (event.value == null) {
        LoadingContent(isLoading = true, error = null) { }
    } else {
        EventDetailContent(eventDetail = event.value!!, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailContent(eventDetail: DetailEvent, viewModel: MainViewModel) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(eventDetail.id) {
        isFavorite = viewModel.isFavorite(eventDetail.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = eventDetail.imageLogo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (isFavorite) {
                                    viewModel.removeFavorite(eventDetail.toFavoriteEvent())
                                    Toast.makeText(
                                        context,
                                        "Removed from favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    viewModel.addFavorite(eventDetail.toFavoriteEvent())
                                    Toast.makeText(
                                        context,
                                        "Added to favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                isFavorite = !isFavorite
                            }
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { shareEvent(context, eventDetail) },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Event",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = eventDetail.name, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Summary: ${eventDetail.summary}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = HtmlCompat.fromHtml(eventDetail.description, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Owner: ${eventDetail.ownerName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "City Name: ${eventDetail.cityName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Quota: ${eventDetail.quota}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Time: ${eventDetail.beginTime} - ${eventDetail.endTime}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Registrants: ${eventDetail.registrants}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Quota - Registrant: ${eventDetail.quota - eventDetail.registrants}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(eventDetail.link))
                        context.startActivity(intent)
                    }) {
                        Text(text = "Go to Event Page", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}

fun shareEvent(context: android.content.Context, eventDetail: DetailEvent) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Check out this event: ${eventDetail.name} - ${eventDetail.description}"
        )
    }

    try {
        context.startActivity(Intent.createChooser(shareIntent, "Share Event Details"))
    } catch (e: Exception) {
        Toast.makeText(context, "No app available to share", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LoadingContent(isLoading: Boolean, error: String?, content: @Composable () -> Unit) {
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error)
            }
        }
        else -> content()
    }
}

fun DetailEvent.toFavoriteEvent(): FavoriteEvent {
    return FavoriteEvent(
        id, name, summary, description, imageLogo, mediaCover, category,
        ownerName, cityName, quota, registrants, beginTime, endTime, link
    )
}
