package com.melikenurozun.recipe_app.domain.repository

import com.melikenurozun.recipe_app.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun getItems(): Flow<List<ShoppingItem>>
    suspend fun addItem(itemName: String): Result<Unit>
    suspend fun toggleItem(item: ShoppingItem): Result<Unit>
    suspend fun deleteItem(id: String): Result<Unit>
}
