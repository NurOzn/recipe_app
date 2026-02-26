package com.melikenurozun.recipe_app.data.repository

import com.melikenurozun.recipe_app.data.remote.dto.ShoppingItemDto
import com.melikenurozun.recipe_app.data.remote.dto.toDomain
import com.melikenurozun.recipe_app.domain.model.ShoppingItem
import com.melikenurozun.recipe_app.domain.repository.ShoppingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ShoppingRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ShoppingRepository {

    private val _refreshFlow = MutableSharedFlow<Unit>(replay = 1)

    init {
        _refreshFlow.tryEmit(Unit)
    }

    override fun getItems(): Flow<List<ShoppingItem>> {
        return _refreshFlow.flatMapLatest {
            flow {
                try {
                    val userId = supabase.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        val items = supabase.from("shopping_items")
                            .select(columns = Columns.ALL) {
                                filter {
                                    eq("user_id", userId)
                                }
                                order("created_at", Order.DESCENDING)
                            }
                            .decodeList<ShoppingItemDto>()
                        emit(items.map { it.toDomain() })
                    } else {
                        emit(emptyList())
                    }
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
    }

    override suspend fun addItem(itemName: String): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            val item = ShoppingItemDto(
                userId = userId,
                itemName = itemName
            )
            supabase.from("shopping_items").insert(item)
            _refreshFlow.emit(Unit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleItem(item: ShoppingItem): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            supabase.from("shopping_items").update(
                mapOf("is_checked" to !item.isChecked)
            ) {
                filter {
                    eq("id", item.id)
                    eq("user_id", userId)
                }
            }
            _refreshFlow.emit(Unit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            supabase.from("shopping_items").delete {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            _refreshFlow.emit(Unit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
