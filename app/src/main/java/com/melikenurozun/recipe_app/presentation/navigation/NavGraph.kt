package com.melikenurozun.recipe_app.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.melikenurozun.recipe_app.presentation.auth.AuthScreen
import com.melikenurozun.recipe_app.presentation.home.HomeScreen
import com.melikenurozun.recipe_app.presentation.detail.DetailScreen
import com.melikenurozun.recipe_app.presentation.add_edit.AddEditScreen
import com.melikenurozun.recipe_app.presentation.search.SearchScreen
import com.melikenurozun.recipe_app.presentation.profile.ProfileScreen
import com.melikenurozun.recipe_app.presentation.favorites.FavoritesScreen
import com.melikenurozun.recipe_app.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.Auth
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home()) {
                        popUpTo(Screen.Auth) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Home> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Home>()
            HomeScreen(
                initialCategory = args.category,
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail(id)) },
                onNavigateToAdd = { navController.navigate(Screen.AddEdit(null)) },
                onNavigateToAuth = {
                     navController.navigate(Screen.Auth) {
                        popUpTo(Screen.Home()) { inclusive = true }
                    }
                },
                onNavigateToSearch = { navController.navigate(Screen.Search) },
                onNavigateToShopping = { navController.navigate(Screen.ShoppingList) },
                onNavigateToProfile = { navController.navigate(Screen.Profile(null)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) }
            )
        }
        
        composable<Screen.Detail> {
            DetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.AddEdit(id)) },
                onNavigateToProfile = { userId -> navController.navigate(Screen.Profile(userId)) },
                onNavigateToCooking = { id -> navController.navigate(Screen.CookingMode(id)) }
            )
        }
        
        composable<Screen.AddEdit> { backStackEntry ->
            AddEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.Search> {
            SearchScreen(
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail(id)) }
            )
        }
        
        composable<Screen.Profile> {
            ProfileScreen(
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail(id)) },
                onNavigateToAuth = {
                     navController.navigate(Screen.Auth) {
                        popUpTo(Screen.Home()) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Favorites> {
            FavoritesScreen(
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail(id)) }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onSignOut = {
                    navController.navigate(Screen.Auth) {
                        popUpTo(Screen.Home()) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.CookingMode> {
            com.melikenurozun.recipe_app.presentation.cooking.CookingModeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ShoppingList> {
            com.melikenurozun.recipe_app.presentation.shopping.ShoppingListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
