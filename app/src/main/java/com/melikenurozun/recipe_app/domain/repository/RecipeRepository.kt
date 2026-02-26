package com.melikenurozun.recipe_app.domain.repository

import com.melikenurozun.recipe_app.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import com.melikenurozun.recipe_app.data.local.FavoriteEntity

interface RecipeRepository {
    suspend fun getRecipes(): List<Recipe>
    suspend fun getRecipeById(id: String): Recipe?
    suspend fun createRecipe(recipe: Recipe)
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: String)
    suspend fun searchRecipes(query: String): List<Recipe>
    
    // Favorites
    fun getFavorites(): Flow<List<Recipe>>
    suspend fun isFavorite(id: String): Boolean
    suspend fun toggleFavorite(recipe: Recipe)
    
    // Ratings
    suspend fun rateRecipe(recipeId: String, rating: Int): Result<Unit>
    suspend fun getUserRating(recipeId: String): Int

    // Image Upload
    suspend fun uploadRecipeImage(imageBytes: ByteArray, fileExtension: String = "jpg"): Result<String>
}
