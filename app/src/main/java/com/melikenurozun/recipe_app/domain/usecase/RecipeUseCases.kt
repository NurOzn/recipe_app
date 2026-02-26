package com.melikenurozun.recipe_app.domain.usecase

import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import com.melikenurozun.recipe_app.data.local.FavoriteEntity

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(): List<Recipe> {
        return repository.getRecipes()
    }
}

class GetRecipeByIdUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: String): Recipe? {
        return repository.getRecipeById(id)
    }
}

class CreateRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.createRecipe(recipe)
    }
}

class UpdateRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.updateRecipe(recipe)
    }
}

class DeleteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteRecipe(id)
    }
}

class SearchRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(query: String): List<Recipe> {
        return repository.searchRecipes(query)
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.toggleFavorite(recipe)
    }
}

class GetFavoritesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.getFavorites()
    }
}

class IsFavoriteUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(id: String): Boolean {
        return repository.isFavorite(id)
    }
}
