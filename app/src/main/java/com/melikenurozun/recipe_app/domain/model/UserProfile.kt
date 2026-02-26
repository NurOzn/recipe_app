package com.melikenurozun.recipe_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String? = null,
    val avatarUrl: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val isMe: Boolean = false
)
