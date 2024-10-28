import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mosalab.submission_awal_faa.Data.DetailEvent
import com.mosalab.submission_awal_faa.Data.FavoriteEvent
import com.mosalab.submission_awal_faa.EventCard
import com.mosalab.submission_awal_faa.MainViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteEventsScreen(viewModel: MainViewModel, navController: NavController) {
    val favoriteEvents by viewModel.favoriteEvents.observeAsState(emptyList())
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Favorite Events") })
        },
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
                    items(favoriteEvents) { event ->
                        EventCard(
                            event = event.toDetailEvent(),
                            navController = navController,
                            isActive = false,
                            viewModel =viewModel,
                            onFavoriteClick = {
                                viewModel.removeFavorite(event)

                                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    )
}

fun FavoriteEvent.toDetailEvent(): DetailEvent {
    return DetailEvent(
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
}
