package com.melikenurozun.recipe_app.domain.repository

import com.melikenurozun.recipe_app.domain.model.Comment

interface CommentRepository {
    suspend fun getComments(recipeId: String): List<Comment>
    suspend fun addComment(recipeId: String, content: String): Result<Unit>
    suspend fun deleteComment(commentId: String): Result<Unit>
    suspend fun toggleLike(commentId: String): Result<Boolean> // Returns true if liked, false if unliked
}
