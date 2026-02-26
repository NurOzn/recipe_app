package com.melikenurozun.recipe_app.data.repository

import com.melikenurozun.recipe_app.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ProfileRepository {

    override suspend fun uploadAvatar(byteArray: ByteArray): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val bucket = supabase.storage.from("avatars")
            bucket.upload(fileName, byteArray)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvatarUrl(url: String): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
            // Update auth metadata
            supabase.auth.updateUser {
                data = buildJsonObject {
                    put("avatar_url", url)
                }
            }
            // Also update public profiles table if it exists (which we created in SQL)
             supabase.from("profiles").update(
                {
                   set("avatar_url", url)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(userId: String): Result<Unit> {
        // Placeholder implementation
        return Result.success(Unit)
    }
}
