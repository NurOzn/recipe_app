package com.melikenurozun.recipe_app.data.repository

import com.melikenurozun.recipe_app.domain.model.Comment
import com.melikenurozun.recipe_app.domain.repository.CommentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : CommentRepository {

    override suspend fun getComments(recipeId: String): List<Comment> {
        return try {
            // Join with profiles to get username? (Assuming profiles table exists and RLS allows reading)
            // Or use auth metadata if we can join auth.users (Supabase doesn't allow joining auth.users easily from client)
            // We'll assume a 'profiles' public table exists or we stored username in comments (denormalized) or just show "User".
            // Implementation plan said "comments table has user_id".
            // Phase 2 DB update didn't create profiles table, but Phase 1 might have or we assume it exists.
            // Let's assume we fetch comments first.
            
            // Wait, we need username.
            // Option 1: Fetch comments, then fetch profiles.
            // Option 2: Join if profiles public table exists.
            // Let's check if profiles table exists. I'll stick to fetching just comments for now and maybe only show content/rating.
            
            val comments = supabase.from("comments")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("recipe_id", recipeId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CommentDto>()
            
            val commentIds = comments.mapNotNull { it.id }
            val userId = supabase.auth.currentUserOrNull()?.id
            
            val likes = if (commentIds.isNotEmpty()) {
                 supabase.from("comment_likes").select {
                     filter {
                         isIn("comment_id", commentIds)
                     }
                 }.decodeList<CommentLikeDto>()
            } else emptyList()

            comments.map { comment ->
                val commentLikes = likes.filter { it.commentId == comment.id }
                val likeCount = commentLikes.size
                val isLikedByMe = if (userId != null) commentLikes.any { it.userId == userId } else false
                comment.toDomain(likeCount, isLikedByMe)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun addComment(recipeId: String, content: String): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
            
            // Generate ID and Timestamp manually to satisfy DB constraints
            val newId = java.util.UUID.randomUUID().toString()
            val currentTimestamp = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                java.time.Instant.now().toString()
            } else {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())
            }

            val comment = CommentDto(
                id = newId,
                userId = userId,
                recipeId = recipeId,
                content = content,
                createdAt = currentTimestamp
            )
            supabase.from("comments").insert(comment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(commentId: String): Result<Boolean> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
            
            // Check if already liked
            val existingLike = supabase.from("comment_likes").select {
                filter {
                    eq("user_id", userId)
                    eq("comment_id", commentId)
                }
            }.decodeList<CommentLikeDto>().firstOrNull()

            if (existingLike != null) {
                // Unlike
                supabase.from("comment_likes").delete {
                    filter {
                        eq("id", existingLike.id)
                    }
                }
                Result.success(false)
            } else {
                // Like
                val newLike = CommentLikeDto(userId = userId, commentId = commentId)
                supabase.from("comment_likes").insert(newLike)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
             supabase.from("comments").delete {
                 filter {
                     eq("id", commentId)
                 }
             }
             Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class CommentDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("recipe_id") val recipeId: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null,
    // Note: We are not fetching joined likes in DTO yet to avoid complex join syntax issues.
    // We will populate likes in Repository or ViewModel.
)

@Serializable
data class CommentLikeDto(
    val id: String = java.util.UUID.randomUUID().toString(),
    @SerialName("user_id") val userId: String,
    @SerialName("comment_id") val commentId: String
)

fun CommentDto.toDomain(likeCount: Int = 0, isLikedByMe: Boolean = false): Comment {
    return Comment(
        id = id ?: "",
        user_id = userId,
        recipe_id = recipeId,
        content = content,
        rating = 0,
        created_at = createdAt,
        like_count = likeCount,
        is_liked_by_me = isLikedByMe
    )
}
