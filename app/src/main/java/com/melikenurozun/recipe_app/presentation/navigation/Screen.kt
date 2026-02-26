package com.melikenurozun.recipe_app.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Auth : Screen()

    @Serializable
    data class Home(val category: String? = null) : Screen()

    @Serializable
    data class Detail(val recipeId: String) : Screen()

    @Serializable
    data class AddEdit(val recipeId: String? = null) : Screen()

    @Serializable
    data object Search : Screen()

    @Serializable
    data class Profile(val userId: String? = null) : Screen()

    @Serializable
    data object Favorites : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data class CookingMode(val recipeId: String) : Screen()

    @Serializable
    data object ShoppingList : Screen()
}
