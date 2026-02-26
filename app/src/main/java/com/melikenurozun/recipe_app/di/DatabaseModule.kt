package com.melikenurozun.recipe_app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.melikenurozun.recipe_app.data.local.RecipeDatabase
import com.melikenurozun.recipe_app.data.local.FavoriteDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRecipeDatabase(
        @ApplicationContext context: Context
    ): RecipeDatabase {
        return Room.databaseBuilder(
            context,
            RecipeDatabase::class.java,
            "recipe_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: RecipeDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}
