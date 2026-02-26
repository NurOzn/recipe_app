package com.melikenurozun.recipe_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(
    @SerialName("user_id") val userId: String,
    @SerialName("recipe_id") val recipeId: String,
    val rating: Int
)
