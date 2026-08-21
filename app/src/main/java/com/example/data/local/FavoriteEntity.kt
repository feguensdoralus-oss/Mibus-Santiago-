package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "BUS", "PARADERO", "METRO"
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_bip_cards")
data class SavedBipCardEntity(
    @PrimaryKey val cardNumber: String,
    val alias: String,
    val lastKnownBalance: Int = 0,
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)
