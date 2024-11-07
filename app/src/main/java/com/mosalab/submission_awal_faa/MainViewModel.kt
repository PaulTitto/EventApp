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
import toDetailEvent

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
        loadFavorites()
        validateFavorites()
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
                validateFavorites()
            } catch (e: Exception) {
                state.value = state.value.copy(
                    loading = false,
                    error = "Error fetching events: ${e.message}"
                )
            }
        }
    }

    private fun validateFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentEventIds = eventActiveState.value.list.map { it.id } +
                    eventNonActiveState.value.list.map { it.id }
            val favoritesToRemove = _favoriteEvents.value.filter { it.id !in currentEventIds }

            favoritesToRemove.forEach { removeFavorite(it) }
        }
    }

    private val favoriteDao = database.favoriteEventDao()
    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            database.favoriteEventDao().getAllFavorites()
                .distinctUntilChanged()
                .collect { favorites ->
                    Log.d("MainViewModel", "Loaded favorites: $favorites")
                    _favoriteEvents.value = favorites
                }
        }
    }



    fun addFavorite(event: FavoriteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("MainViewModel", "Adding to favorites: $event")
                database.favoriteEventDao().insertFavorite(event)
                loadFavorites()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error adding to favorites: ${e.message}")
            }
        }
    }

    fun removeFavorite(event: FavoriteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            database.favoriteEventDao().deleteFavorite(event)
            loadFavorites()
        }
    }
    suspend fun getFavoriteEventById(eventId: Int): DetailEvent? {
        return withContext(Dispatchers.IO) {
            database.favoriteEventDao().getFavoriteEventById(eventId)?.toDetailEvent()
        }
    }

    suspend fun isFavorite(eventId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            database.favoriteEventDao().isFavorite(eventId)
        }
    }

    data class DetailEventState(
        val loading: Boolean = true,
        val list: List<DetailEvent> = emptyList(),
        val error: String? = null
    )
}
