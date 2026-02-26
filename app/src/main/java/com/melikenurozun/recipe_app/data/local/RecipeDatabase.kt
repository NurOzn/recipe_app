package com.melikenurozun.recipe_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
