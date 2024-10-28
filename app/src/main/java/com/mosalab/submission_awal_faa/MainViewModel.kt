package com.mosalab.submission_awal_faa

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.mosalab.submission_awal_faa.Data.AppDatabase
import com.mosalab.submission_awal_faa.Data.DetailEvent
import com.mosalab.submission_awal_faa.Data.FavoriteEvent
import com.mosalab.submission_awal_faa.Service.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isDarkMode: Flow<Boolean> = preferencesManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
        }
    }

    private val _eventActiveState = mutableStateOf(DetailEventState())
    private val _eventNonActiveState = mutableStateOf(DetailEventState())

    val eventActiveState: State<DetailEventState> = _eventActiveState
    val eventNonActiveState: State<DetailEventState> = _eventNonActiveState

    init {
        fetchEvents(active = "1", _eventActiveState)
        fetchEvents(active = "0", _eventNonActiveState)
    }

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
    val favoriteEvents: LiveData<List<FavoriteEvent>> = favoriteDao.getAllFavorites().asLiveData()

    fun addFavorite(event: FavoriteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteDao.insertFavorite(event)
        }
    }

    fun removeFavorite(event: FavoriteEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteDao.deleteFavorite(event)
        }
    }

    suspend fun isFavorite(eventId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteDao.isFavorite(eventId)
        }
    }

    data class DetailEventState(
        val loading: Boolean = true,
        val list: List<DetailEvent> = emptyList(),
        val error: String? = null
    )
}
