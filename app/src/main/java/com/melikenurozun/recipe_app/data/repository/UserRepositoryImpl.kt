package com.melikenurozun.recipe_app.data.repository

import com.melikenurozun.recipe_app.domain.model.UserProfile
import com.melikenurozun.recipe_app.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : UserRepository {

    override suspend fun getProfile(userId: String): Result<UserProfile> {
         return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
            
            // 1. Fetch Profile Data from 'profiles' table
            val profileDto = supabase.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<ProfileDto>() ?: throw Exception("User not found")

            // 2. Fetch Follower Count
            val followerCount = supabase.from("follows").select {
                count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                filter { eq("following_id", userId) }
            }.countOrNull() ?: 0

            // 3. Fetch Following Count
            val followingCount = supabase.from("follows").select {
                 count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                 filter { eq("follower_id", userId) }
            }.countOrNull() ?: 0

            // 4. Check if I am following
            val isFollowing = if (currentUserId != null && currentUserId != userId) {
                 supabase.from("follows").select {
                     count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                     filter { 
                         eq("follower_id", currentUserId)
                         eq("following_id", userId)
                     }
                 }.countOrNull() ?: 0 > 0
            } else {
                false
            }

            val isMe = currentUserId == userId

            val userProfile = UserProfile(
                id = userId,
                username = profileDto.username,
                avatarUrl = profileDto.avatarUrl,
                followerCount = followerCount.toInt(),
                followingCount = followingCount.toInt(),
                isFollowing = isFollowing,
                isMe = isMe
            )
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFollow(userId: String): Result<Boolean> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            
            // Check if already following
            val existingFollow = supabase.from("follows").select {
                 filter {
                     eq("follower_id", currentUserId)
                     eq("following_id", userId)
                 }
            }.decodeSingleOrNull<FollowDto>()

            if (existingFollow != null) {
                // Unfollow
                supabase.from("follows").delete {
                     filter { eq("id", existingFollow.id) }
                }
                Result.success(false)
            } else {
                // Follow
                val follow = FollowDto(followerId = currentUserId, followingId = userId)
                supabase.from("follows").insert(follow)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFollowing(userId: String): Result<Boolean> {
        // Implemented inside getProfile, but can be standalone
        return try {
             val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return Result.success(false)
             val count = supabase.from("follows").select {
                 count(io.github.jan.supabase.postgrest.query.Count.EXACT)
                 filter { 
                     eq("follower_id", currentUserId)
                     eq("following_id", userId)
                 }
             }.countOrNull() ?: 0
             Result.success(count > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAvatar(url: String): Result<Unit> {
         return try {
             // We update auth metadata AND profiles table
             supabase.auth.updateUser {
                 data = kotlinx.serialization.json.buildJsonObject {
                     put("avatar_url", kotlinx.serialization.json.JsonPrimitive(url))
                 }
             }
             // Trigger should handle profiles update if we insert new user, but for UPDATE we might need manual update or another trigger.
             // Our setup had trigger on INSERT. For UPDATE, let's manually update profiles table for safety.
             val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
             supabase.from("profiles").update({
                 set("avatar_url", url)
             }) {
                 filter { eq("id", userId) }
             }
             Result.success(Unit)
         } catch(e: Exception) {
             Result.failure(e)
         }
    }

    override suspend fun updateUsername(username: String): Result<Unit> {
         return try {
             supabase.auth.updateUser {
                 data = kotlinx.serialization.json.buildJsonObject {
                     put("full_name", kotlinx.serialization.json.JsonPrimitive(username))
                 }
             }
             val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
             supabase.from("profiles").update({
                 set("username", username)
             }) {
                 filter { eq("id", userId) }
             }
             Result.success(Unit)
         } catch(e: Exception) {
             Result.failure(e)
         }

    }

    override suspend fun getFollowedUserIds(): Result<List<String>> {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return Result.success(emptyList())
            val follows = supabase.from("follows").select {
                filter {
                    eq("follower_id", currentUserId)
                }
            }.decodeList<FollowDto>()
            Result.success(follows.map { it.followingId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class ProfileDto(
    val id: String,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val email: String? = null
)

@Serializable
data class FollowDto(
    val id: String = java.util.UUID.randomUUID().toString(),
    @SerialName("follower_id") val followerId: String,
    @SerialName("following_id") val followingId: String
)
