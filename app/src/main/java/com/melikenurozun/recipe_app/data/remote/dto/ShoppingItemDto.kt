package com.melikenurozun.recipe_app.data.remote.dto

import com.melikenurozun.recipe_app.domain.model.ShoppingItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItemDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("item_name") val itemName: String,
    @SerialName("is_checked") val isChecked: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

fun ShoppingItemDto.toDomain(): ShoppingItem {
    return ShoppingItem(
        id = id ?: "",
        userId = userId,
        itemName = itemName,
        isChecked = isChecked
    )
}
