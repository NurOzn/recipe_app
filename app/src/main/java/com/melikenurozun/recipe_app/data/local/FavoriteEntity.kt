package com.melikenurozun.recipe_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val ingredients: String,
    val instructions: String,
    val imageUrl: String?
)
