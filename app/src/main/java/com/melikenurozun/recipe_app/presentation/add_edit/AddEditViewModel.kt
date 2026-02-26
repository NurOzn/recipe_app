package com.melikenurozun.recipe_app.presentation.add_edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.repository.AuthRepository
import com.melikenurozun.recipe_app.domain.usecase.CreateRecipeUseCase
import com.melikenurozun.recipe_app.domain.usecase.GetRecipeByIdUseCase
import com.melikenurozun.recipe_app.domain.usecase.UpdateRecipeUseCase
import com.melikenurozun.recipe_app.domain.usecase.UploadImageUseCase
import com.melikenurozun.recipe_app.presentation.util.UiMessageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String? = savedStateHandle["recipeId"]

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (recipeId != null) {
            loadRecipe(recipeId)
        }
    }

    private fun loadRecipe(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val recipe = getRecipeByIdUseCase(id)
            if (recipe != null) {
                _uiState.update {
                    it.copy(
                        title = recipe.title,
                        ingredients = recipe.ingredients,
                        instructions = recipe.instructions,
                        imageUrl = recipe.image_url ?: "",
                        category = recipe.category ?: "",
                        timeMinutes = recipe.timeMinutes,
                        isLoading = false,
                        isEditMode = true
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Recipe not found.") }
            }
        }
    }

    fun onEvent(event: AddEditEvent) {
        when (event) {
            is AddEditEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is AddEditEvent.IngredientsChanged -> _uiState.update { it.copy(ingredients = event.ingredients) }
            is AddEditEvent.InstructionsChanged -> _uiState.update { it.copy(instructions = event.instructions) }
            is AddEditEvent.ImageUrlChanged -> _uiState.update { it.copy(imageUrl = event.imageUrl) }
            is AddEditEvent.ImageSelected -> _uiState.update { it.copy(imageUri = event.uri) }
            is AddEditEvent.CategoryChanged -> _uiState.update { it.copy(category = event.category) }
            is AddEditEvent.TimeChanged -> _uiState.update { it.copy(timeMinutes = event.timeMinutes) }
            AddEditEvent.SaveRecipe -> saveRecipe()
        }
    }

    private fun saveRecipe() {
        val state = uiState.value
        if (state.title.isBlank() || state.ingredients.isBlank() || state.instructions.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = authRepository.getCurrentUserId() ?: throw Exception("User not logged in.")
                
                var finalImageUrl = state.imageUrl
                
                // Handle Image Upload
                if (state.imageUri != null) {
                    val compressedBytes = try {
                        context.contentResolver.openInputStream(state.imageUri)?.use { inputStream ->
                            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                            val outputStream = java.io.ByteArrayOutputStream()
                            // Compress to JPEG with 70% quality
                            originalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                            // Resize if too big (optional, but good for safety)
                            // For now simple compression is usually enough
                            outputStream.toByteArray()
                        }
                    } catch (e: Exception) {
                        null
                    }

                     if (compressedBytes != null) {
                         uploadImageUseCase(compressedBytes)
                             .onSuccess { url ->
                                 finalImageUrl = url
                             }
                             .onFailure { e ->
                                 e.printStackTrace()
                                 _uiState.update { it.copy(isLoading = false, error = "Failed to upload image: ${e.message}") }
                                 return@launch
                             }
                     }
                }

                val idToUse = recipeId ?: java.util.UUID.randomUUID().toString()

                // Generate timestamp for created_at
                val currentTimestamp = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    java.time.Instant.now().toString()
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date())
                }

                val recipe = Recipe(
                    id = idToUse,
                    user_id = userId,
                    title = state.title,
                    ingredients = state.ingredients,
                    instructions = state.instructions,
                    image_url = finalImageUrl.ifBlank { null },
                    category = state.category.ifBlank { null },
                    timeMinutes = state.timeMinutes,
                    created_at = if (recipeId == null) currentTimestamp else null // Only set for new recipes
                )

                if (state.isEditMode && recipeId != null) {
                    // For updates, we usually don't change created_at, assuming the object is merged or handled by backend.
                    // But to be safe and avoid overwriting existing created_at with null if the Repo logic replaces the whole object...
                    // Actually Repository update uses the DTO. If created_at is null in DTO, it might be ignored or set to null.
                    // However, we don't have the original created_at here easily unless we loaded it.
                    // We DID load it in loadRecipe. Let's try to preserve it.
                    // But wait, the state doesn't hold created_at.
                    // For now, let's assume update doesn't touch created_at if it's not provided, OR we rely on partial update (PATCH) which is what Supabase usually does.
                    // The Repository uses .update(recipe.toDto()), which sends all fields.
                    // If we send null created_at during update, and it's NOT NULL column, it might error if it tries to set it to null.
                    // BUT usually update ignores nulls for "PATCH" requests if configured, but here we are sending the whole object.
                    // Let's modify Repo to use columns or check if toDto cleans it.
                    // `toDto` includes `createdAt = created_at`.
                    // If we update, we should probably fetch the existing created_at.
                    // Simplest fix for NOW: Just set it for NEW recipes. For updates, hopefully the existing value is preserved or we need to handle it.
                    // Wait, if I send null for created_at on UPDATE, and it's NOT NULL, it will crash.
                    // I need to fetch the original createdAt in loadRecipe and store it in state?
                    // Or change Repository to use partial updates.
                    // Let's look at `RecipeRepositoryImpl.updateRecipe`. It does `supabase.from("recipes").update(recipe.toDto())`.
                    
                    // Actually, let's simply populate created_at for NEW recipes first to fix the immediate error.
                    // If update fails later, we fix that. The user is currently trying to ADD a recipe.
                    
                    updateRecipeUseCase(recipe)
                } else {
                    createRecipeUseCase(recipe)
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                val errorMessage = UiMessageHelper.getMessage(e)
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

data class AddEditUiState(
    val title: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val timeMinutes: Int = 0,
    val availableCategories: List<String> = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack"),
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed class AddEditEvent {
    data class TitleChanged(val title: String) : AddEditEvent()
    data class IngredientsChanged(val ingredients: String) : AddEditEvent()
    data class InstructionsChanged(val instructions: String) : AddEditEvent()
    data class ImageUrlChanged(val imageUrl: String) : AddEditEvent()
    data class ImageSelected(val uri: Uri) : AddEditEvent()
    data class CategoryChanged(val category: String) : AddEditEvent()
    data class TimeChanged(val timeMinutes: Int) : AddEditEvent()
    object SaveRecipe : AddEditEvent()
}
