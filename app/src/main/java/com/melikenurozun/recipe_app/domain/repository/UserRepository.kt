package com.melikenurozun.recipe_app.domain.repository

import com.melikenurozun.recipe_app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun toggleFollow(userId: String): Result<Boolean> // Returns true if followed, false if unfollowed
    suspend fun isFollowing(userId: String): Result<Boolean>
    suspend fun updateAvatar(url: String): Result<Unit>
    suspend fun updateUsername(username: String): Result<Unit>
    suspend fun getFollowedUserIds(): Result<List<String>>
}
