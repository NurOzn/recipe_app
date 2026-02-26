package com.melikenurozun.recipe_app.domain.usecase

import com.melikenurozun.recipe_app.domain.repository.RecipeRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(imageBytes: ByteArray): Result<String> {
        return repository.uploadRecipeImage(imageBytes)
    }
}
