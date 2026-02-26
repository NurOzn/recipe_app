package com.melikenurozun.recipe_app.data.remote.dto

import com.melikenurozun.recipe_app.data.remote.RecipeDto
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteDto(
    val recipes: RecipeDto? = null
)
