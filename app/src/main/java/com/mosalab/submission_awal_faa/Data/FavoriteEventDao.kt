package com.mosalab.submission_awal_faa.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(event: FavoriteEvent)

    @Delete
    fun deleteFavorite(event: FavoriteEvent)

    @Query("SELECT * FROM favorite_events")
    fun getAllFavorites(): Flow<List<FavoriteEvent>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_events WHERE id = :id)")
    fun isFavorite(id: Int): Boolean

    @Query("SELECT * FROM favorite_events WHERE id = :id")
    fun getFavoriteEventById(id: Int): FavoriteEvent?

}
