package com.melikenurozun.recipe_app.domain.model

data class ShoppingItem(
    val id: String = "",
    val userId: String = "",
    val itemName: String,
    val isChecked: Boolean = false
)
