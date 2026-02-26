package com.melikenurozun.recipe_app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val settingsRepository: com.melikenurozun.recipe_app.data.repository.SettingsRepository,
    private val userRepository: com.melikenurozun.recipe_app.domain.repository.UserRepository,
    private val authRepository: com.melikenurozun.recipe_app.domain.repository.AuthRepository,
    private val recipeRepository: com.melikenurozun.recipe_app.domain.repository.RecipeRepository
) : ViewModel() {

    // ... existing ...

    fun toggleFavorite(recipeId: String) {
        val currentRecipes = _uiState.value.recipes
        val index = currentRecipes.indexOfFirst { it.id == recipeId }
        if (index != -1) {
            val recipe = currentRecipes[index]
            val newIsFavorite = !recipe.isFavorite
            
            // Optimistic Update
            val updatedRecipe = recipe.copy(isFavorite = newIsFavorite)
            val updatedList = currentRecipes.toMutableList().apply {
                set(index, updatedRecipe)
            }
            _uiState.update { it.copy(recipes = updatedList) }
            
            // API Call
            viewModelScope.launch {
                try {
                    recipeRepository.toggleFavorite(recipe)
                } catch (e: Exception) {
                    // Revert on failure
                     val revertedList = _uiState.value.recipes.toMutableList().apply {
                        val revertIndex = indexOfFirst { it.id == recipeId }
                        if (revertIndex != -1) {
                            set(revertIndex, recipe)
                        }
                    }
                     _uiState.update { it.copy(recipes = revertedList) }
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allRecipesCache: List<Recipe> = emptyList()
    private var followedUserIds: List<String> = emptyList()

    init {
        loadRecipes()
        viewModelScope.launch {
             settingsRepository.isGuestMode.collect { isGuest ->
                 _uiState.update { it.copy(isGuest = isGuest) }
             }
        }
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch recipes
                val recipes = getRecipesUseCase()
                allRecipesCache = recipes
                
                // Fetch followed users if logged in
                if (authRepository.getCurrentUserId() != null) {
                    userRepository.getFollowedUserIds().onSuccess { ids ->
                        followedUserIds = ids
                    }
                }
                
                applyFilters()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to load recipes", isLoading = false) }
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { state ->
            val newCategory = if (state.selectedCategory == category) null else category
            state.copy(selectedCategory = newCategory)
        }
        applyFilters()
    }
    
    fun onFilterSelected(filter: FeedFilter) {
        // If guest tries to select FOLLOWING, maybe ShowToast? The UI should probably disable it or show dialog.
        // Assuming UI handles guest check or we silently ignore.
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val category = currentState.selectedCategory
        val filter = currentState.selectedFilter
        
        var result = allRecipesCache
        
        // 1. Filter by Feed Type
        if (filter == FeedFilter.FOLLOWING) {
             result = result.filter { followedUserIds.contains(it.user_id) }
        }
        
        // 2. Filter by Category
        if (category != null) {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }
        
        _uiState.update { it.copy(recipes = result) }
    }
}

enum class FeedFilter {
    ALL, FOLLOWING
}

data class HomeUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGuest: Boolean = false,
    val selectedCategory: String? = null,
    val selectedFilter: FeedFilter = FeedFilter.ALL,
    val categories: List<String> = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack")
)
