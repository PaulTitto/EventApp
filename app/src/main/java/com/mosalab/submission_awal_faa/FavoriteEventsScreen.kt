import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mosalab.submission_awal_faa.Data.DetailEvent
import com.mosalab.submission_awal_faa.Data.FavoriteEvent
import com.mosalab.submission_awal_faa.MainViewModel
import com.mosalab.submission_awal_faa.EventCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteEventsScreen(viewModel: MainViewModel, navController: NavController) {
    val favoriteEvents by viewModel.favoriteEvents.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Favorite Events") }) },
        content = { paddingValues ->
            if (favoriteEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorite events found.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(paddingValues)
                ) {
                    // Log the list to ensure correct data is being passed
                    Log.d("FavoriteEventsScreen", "Rendering favorites: $favoriteEvents")

                    items(favoriteEvents) { event ->
                        EventCard(
                            event = event.toDetailEvent(),
                            navController = navController,
                            isActive = event.isActive(),
                            viewModel = viewModel,
                            coroutineScope = coroutineScope,
                            context = context
                        )
                    }
                }
            }
        }
    )
}


fun FavoriteEvent.toDetailEvent(): DetailEvent {
    val detailEvent = DetailEvent(
        id = id,
        name = name,
        summary = summary,
        description = description,
        imageLogo = imageLogo,
        mediaCover = mediaCover,
        category = category,
        ownerName = ownerName,
        cityName = cityName,
        quota = quota,
        registrants = registrants,
        beginTime = beginTime,
        endTime = endTime,
        link = link
    )
    Log.d("FavoriteEvent", "Converted to DetailEvent: $detailEvent") // Log conversion
    return detailEvent
}



// Check if the FavoriteEvent is active
fun FavoriteEvent.isActive(): Boolean {
    return (quota - registrants) > 0
}
