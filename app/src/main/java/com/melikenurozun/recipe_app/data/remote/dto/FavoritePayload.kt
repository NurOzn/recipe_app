package com.melikenurozun.recipe_app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FavoritePayload(
    val user_id: String,
    val recipe_id: String
)
