package com.melikenurozun.recipe_app.presentation.cooking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.usecase.GetRecipeByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CookingModeViewModel @Inject constructor(
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(CookingUiState())
    val uiState: StateFlow<CookingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            val recipe = getRecipeByIdUseCase(recipeId)
            if (recipe != null) {
                // Split instructions into steps
                // Assuming instructions are unified text, we split by newline
                val steps = recipe.instructions.lineSequence()
                    .filter { it.isNotBlank() }
                    .toList()
                
                _uiState.update { 
                    it.copy(
                        recipe = recipe, 
                        steps = steps,
                        currentStepIndex = 0
                    ) 
                }
            }
        }
    }

    fun onEvent(event: CookingEvent) {
        when (event) {
            CookingEvent.NextStep -> {
                _uiState.update {
                    if (it.currentStepIndex < it.steps.lastIndex) {
                        it.copy(currentStepIndex = it.currentStepIndex + 1)
                    } else it
                }
            }
            CookingEvent.PreviousStep -> {
                _uiState.update {
                    if (it.currentStepIndex > 0) {
                        it.copy(currentStepIndex = it.currentStepIndex - 1)
                    } else it
                }
            }
            is CookingEvent.StartTimer -> {
                startTimer(event.minutes)
            }
            CookingEvent.StopTimer -> {
                stopTimer()
            }
            CookingEvent.ToggleKeepScreenOn -> {
                _uiState.update { it.copy(keepScreenOn = !it.keepScreenOn) }
            }
        }
    }

    private fun startTimer(minutes: Int) {
        stopTimer()
        val totalSeconds = minutes * 60L
        _uiState.update { it.copy(isTimerRunning = true, timerSeconds = totalSeconds, initialTimerSeconds = totalSeconds) }
        
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
            _uiState.update { it.copy(isTimerRunning = false, timerSeconds = 0) }
            // Optional: Play sound or vibrate
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
    }
}

data class CookingUiState(
    val recipe: Recipe? = null,
    val steps: List<String> = emptyList(),
    val currentStepIndex: Int = 0,
    val isTimerRunning: Boolean = false,
    val timerSeconds: Long = 0,
    val initialTimerSeconds: Long = 0,
    val keepScreenOn: Boolean = true
)

sealed class CookingEvent {
    object NextStep : CookingEvent()
    object PreviousStep : CookingEvent()
    data class StartTimer(val minutes: Int) : CookingEvent()
    object StopTimer : CookingEvent()
    object ToggleKeepScreenOn : CookingEvent()
}
