package com.melikenurozun.recipe_app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.usecase.DeleteRecipeUseCase
import com.melikenurozun.recipe_app.domain.usecase.GetRecipeByIdUseCase
import com.melikenurozun.recipe_app.domain.usecase.IsFavoriteUseCase
import com.melikenurozun.recipe_app.domain.usecase.ToggleFavoriteUseCase
import com.melikenurozun.recipe_app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val rateRecipeUseCase: com.melikenurozun.recipe_app.domain.usecase.RateRecipeUseCase,
    private val getUserRatingUseCase: com.melikenurozun.recipe_app.domain.usecase.GetUserRatingUseCase,
    private val authRepository: AuthRepository,
    private val commentRepository: com.melikenurozun.recipe_app.domain.repository.CommentRepository,
    private val shoppingRepository: com.melikenurozun.recipe_app.domain.repository.ShoppingRepository,
    private val settingsRepository: com.melikenurozun.recipe_app.data.repository.SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = kotlinx.coroutines.flow.MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadRecipe()
        loadComments()
        loadUserRating()
        viewModelScope.launch {
             settingsRepository.isGuestMode.collect { isGuest ->
                 _uiState.update { it.copy(isGuest = isGuest) }
             }
        }
    }
    
    // ... loadRecipe (keep as is) ...
    private fun loadRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val recipe = getRecipeByIdUseCase(recipeId)
            if (recipe != null) {
                val isFavorite = isFavoriteUseCase(recipeId)
                val isOwnRecipe = recipe.user_id == authRepository.getCurrentUserId()
                _uiState.update { 
                    it.copy(
                        recipe = recipe, 
                        isFavorite = isFavorite,
                        isOwnRecipe = isOwnRecipe,
                        isLoading = false 
                    ) 
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Recipe not found.") }
            }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            val comments = commentRepository.getComments(recipeId)
            _uiState.update { it.copy(comments = comments) }
        }
    }

    private fun loadUserRating() {
        viewModelScope.launch {
            val rating = getUserRatingUseCase(recipeId)
            _uiState.update { it.copy(userRating = rating) }
        }
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            DetailEvent.ToggleFavorite -> {
                val recipe = uiState.value.recipe ?: return
                viewModelScope.launch {
                    toggleFavoriteUseCase(recipe)
                    _uiState.update { it.copy(isFavorite = !it.isFavorite) }
                }
            }
            DetailEvent.DeleteRecipe -> {
                val recipe = uiState.value.recipe ?: return
                viewModelScope.launch {
                    try {
                        deleteRecipeUseCase(recipe.id)
                        _uiState.update { it.copy(isDeleted = true) }
                    } catch (e: Exception) {
                         val errorMessage = com.melikenurozun.recipe_app.presentation.util.UiMessageHelper.getMessage(e)
                        _uiState.update { it.copy(error = errorMessage) }
                    }
                }
            }
            is DetailEvent.AddComment -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isCommentLoading = true) }
                    val result = commentRepository.addComment(recipeId, event.content)
                    result.onSuccess {
                        loadComments() // Refresh logic
                        _uiState.update { it.copy(isCommentLoading = false) }
                        _uiEvent.emit(UiEvent.ShowToast("Thank you for your review!"))
                    }.onFailure {
                         val errorMessage = com.melikenurozun.recipe_app.presentation.util.UiMessageHelper.getMessage(it)
                         _uiState.update { it.copy(isCommentLoading = false) } 
                         _uiEvent.emit(UiEvent.ShowToast("Error adding comment: $errorMessage"))
                    }
                }
            }
            is DetailEvent.RateRecipe -> {
                viewModelScope.launch {
                    val result = rateRecipeUseCase(recipeId, event.rating)
                    result.onSuccess {
                        _uiState.update { it.copy(userRating = event.rating) }
                        _uiEvent.emit(UiEvent.ShowToast("Thank you for your rating!"))
                        loadRecipe() 
                    }.onFailure {
                        val errorMessage = com.melikenurozun.recipe_app.presentation.util.UiMessageHelper.getMessage(it)
                        _uiEvent.emit(UiEvent.ShowToast("Error rating recipe: $errorMessage"))
                    }
                }
            }
            is DetailEvent.ToggleCommentLike -> {
                viewModelScope.launch {
                    val result = commentRepository.toggleLike(event.commentId)
                    result.onSuccess {
                        loadComments() // Reload to get updated counts/status
                    }.onFailure {
                         val errorMessage = com.melikenurozun.recipe_app.presentation.util.UiMessageHelper.getMessage(it)
                         _uiEvent.emit(UiEvent.ShowToast("Error: $errorMessage"))
                    }
                }
            }
            is DetailEvent.AddToShoppingList -> {
                viewModelScope.launch {
                    val result = shoppingRepository.addItem(event.ingredient)
                    result.onSuccess {
                        _uiEvent.emit(UiEvent.ShowToast("Added to Shopping List"))
                    }.onFailure {
                        _uiEvent.emit(UiEvent.ShowToast("Error adding to list"))
                    }
                }
            }
        }
    }
}

data class DetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isOwnRecipe: Boolean = false,
    val isDeleted: Boolean = false,
    val comments: List<com.melikenurozun.recipe_app.domain.model.Comment> = emptyList(),
    val isCommentLoading: Boolean = false,
    val userRating: Int = 0,
    val isGuest: Boolean = false
)

sealed class DetailEvent {
    object ToggleFavorite : DetailEvent()
    object DeleteRecipe : DetailEvent()
    data class AddComment(val content: String) : DetailEvent()
    data class RateRecipe(val rating: Int) : DetailEvent()
    data class ToggleCommentLike(val commentId: String) : DetailEvent()
    data class AddToShoppingList(val ingredient: String) : DetailEvent()
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}
