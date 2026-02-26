package com.melikenurozun.recipe_app.domain.repository

import com.melikenurozun.recipe_app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String, username: String? = null): Result<Unit>
    suspend fun signOut()
    suspend fun updateUsername(username: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun getCurrentUserId(): String?
}
