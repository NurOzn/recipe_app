package com.melikenurozun.recipe_app.domain.model

data class User(
    val id: String,
    val email: String,
    val username: String? = null,
    val avatarUrl: String? = null
)
