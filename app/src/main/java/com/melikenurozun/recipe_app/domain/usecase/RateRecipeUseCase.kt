package com.melikenurozun.recipe_app.domain.usecase

import com.melikenurozun.recipe_app.domain.repository.RecipeRepository
import javax.inject.Inject

class RateRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String, rating: Int): Result<Unit> {
        return repository.rateRecipe(recipeId, rating)
    }
}
