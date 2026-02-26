package com.melikenurozun.recipe_app.domain.usecase

import com.melikenurozun.recipe_app.domain.repository.RecipeRepository
import javax.inject.Inject

class GetUserRatingUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Int {
        return repository.getUserRating(recipeId)
    }
}
