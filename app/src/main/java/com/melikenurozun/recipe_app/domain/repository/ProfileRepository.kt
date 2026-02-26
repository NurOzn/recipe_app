package com.melikenurozun.recipe_app.domain.repository

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun uploadAvatar(byteArray: ByteArray): Result<String>
    suspend fun updateAvatarUrl(url: String): Result<Unit>
    suspend fun getProfile(userId: String): Result<Unit> // Placeholder if needed
}
