package com.melikenurozun.recipe_app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.usecase.SearchRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRecipesUseCase: SearchRecipesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        performSearch()
    }

    fun addExcludedIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isNotBlank() && !_uiState.value.excludedIngredients.contains(trimmed)) {
             _uiState.update { it.copy(excludedIngredients = it.excludedIngredients + trimmed) }
             performSearch()
        }
    }

    fun removeExcludedIngredient(ingredient: String) {
        _uiState.update { it.copy(excludedIngredients = it.excludedIngredients - ingredient) }
        performSearch()
    }

    private fun performSearch() {
        val query = _uiState.value.query
        if (query.length < 2) {
             _uiState.update { it.copy(results = emptyList()) }
             return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Basic Search
                val rawResults = searchRecipesUseCase(query)
                
                // 2. Filter Excluded Ingredients
                val excluded = _uiState.value.excludedIngredients
                val filteredResults = rawResults.filter { recipe ->
                    excluded.none { ex -> 
                        recipe.ingredients.contains(ex, ignoreCase = true) || 
                        recipe.title.contains(ex, ignoreCase = true) // Optional: also check title
                    }
                }
                
                _uiState.update { it.copy(results = filteredResults, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class SearchUiState(
    val query: String = "",
    val results: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val excludedIngredients: List<String> = emptyList()
)
