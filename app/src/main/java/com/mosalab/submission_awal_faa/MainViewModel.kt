package com.mosalab.submission_awal_faa

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosalab.submission_awal_faa.Data.AppDatabase
import com.mosalab.submission_awal_faa.Data.DetailEvent
import com.mosalab.submission_awal_faa.Data.FavoriteEvent
import com.mosalab.submission_awal_faa.Service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // Dark mode state
    val isDarkMode: StateFlow<Boolean> = preferencesManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
        }
    }

    // State for active and non-active events
    private val _eventActiveState = mutableStateOf(DetailEventState())
    private val _eventNonActiveState = mutableStateOf(DetailEventState())

    val eventActiveState: State<DetailEventState> = _eventActiveState
    val eventNonActiveState: State<DetailEventState> = _eventNonActiveState

    // StateFlow for favorites
    private val _favoriteEvents = MutableStateFlow<List<FavoriteEvent>>(emptyList())
    val favoriteEvents: StateFlow<List<FavoriteEvent>> = _favoriteEvents.asStateFlow()


    init {
        fetchEvents(active = "1", _eventActiveState)
        fetchEvents(active = "0", _eventNonActiveState)
        loadFavorites() // Load initial favorites
    }

    // Fetch events from API
    private fun fetchEvents(active: String, state: MutableState<DetailEventState>) {
        viewModelScope.launch {
            state.value = state.value.copy(loading = true)
            try {
                val response = ApiService.apiService.getEvents(active)
                state.value = state.value.copy(
                    list = response.listEvents,
                    loading = false,
                    error = null
                )
            } catch (e: Exception) {
                state.value = state.value.copy(
                    loading = false,
                    error = "Error fetching events: ${e.message}"
                )
            }
        }
    }
    private val favoriteDao = database.favoriteEventDao()
    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            database.favoriteEventDao().getAllFavorites().collect { favorites ->
                Log.d("MainViewModel", "Loaded favorites: $favorites") // Log loaded favorites
                _favoriteEvents.value = favorites
            }
        }
    }


    fun addFavorite(event: FavoriteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("MainViewModel", "Adding to favorites: $event") // Log event being added
                database.favoriteEventDao().insertFavorite(event)
                loadFavorites() // Refresh after adding
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error adding to favorites: ${e.message}")
            }
        }
    }



    // Remove favorite and refresh state
    fun removeFavorite(event: FavoriteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            database.favoriteEventDao().deleteFavorite(event)
            loadFavorites() // Refresh after removing
        }
    }

    // Check if the event is a favorite
    suspend fun isFavorite(eventId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            database.favoriteEventDao().isFavorite(eventId)
        }
    }

    // Data class to represent the state of events
    data class DetailEventState(
        val loading: Boolean = true,
        val list: List<DetailEvent> = emptyList(),
        val error: String? = null
    )
}
