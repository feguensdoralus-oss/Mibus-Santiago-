package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT * FROM saved_bip_cards ORDER BY lastCheckedTimestamp DESC")
    fun getSavedBipCards(): Flow<List<SavedBipCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBipCard(card: SavedBipCardEntity)

    @Query("DELETE FROM saved_bip_cards WHERE cardNumber = :cardNumber")
    suspend fun deleteBipCard(cardNumber: String)
}
