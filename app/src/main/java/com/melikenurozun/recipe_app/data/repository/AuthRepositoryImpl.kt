package com.melikenurozun.recipe_app.data.repository

import com.melikenurozun.recipe_app.domain.model.User
import com.melikenurozun.recipe_app.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    init {
        // Ensure session is loaded on startup without blocking the main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.loadFromStorage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override val currentUser: Flow<User?> = supabase.auth.sessionStatus.map {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            User(
                id = user.id,
                email = user.email ?: "",
                username = user.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\""),
                avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.removeSurrounding("\"")
            )
        } else {
            null
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, username: String?): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                if (username != null) {
                    data = kotlinx.serialization.json.buildJsonObject {
                        put("full_name", kotlinx.serialization.json.JsonPrimitive(username))
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateUsername(username: String): Result<Unit> {
        return try {
            supabase.auth.updateUser {
                data = kotlinx.serialization.json.buildJsonObject {
                   put("full_name", kotlinx.serialization.json.JsonPrimitive(username))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
}
