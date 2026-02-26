package com.melikenurozun.recipe_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val user_id: String,
    val recipe_id: String,
    val content: String,
    val rating: Int,
    val created_at: String? = null,
    val username: String? = null,
    val like_count: Int = 0,
    val is_liked_by_me: Boolean = false,
    val reply_count: Int = 0
)
