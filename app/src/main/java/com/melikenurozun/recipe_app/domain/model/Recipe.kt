package com.melikenurozun.recipe_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String = "",
    val user_id: String = "",
    val title: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val image_url: String? = null,
    val category: String? = null,
    val average_rating: Double = 0.0,
    val rating_count: Int = 0,
    val created_at: String? = null,
    val username: String? = null,
    val timeMinutes: Int = 0,
    val isFavorite: Boolean = false
)
