package com.melikenurozun.recipe_app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.data.repository.SettingsRepository
import com.melikenurozun.recipe_app.domain.usecase.SignInUseCase
import com.melikenurozun.recipe_app.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val settingsRepository: SettingsRepository,
    private val authRepository: com.melikenurozun.recipe_app.domain.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.rememberMe.collect { remember ->
                _uiState.update { it.copy(rememberMe = remember) }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> _uiState.update { it.copy(email = event.email) }
            is AuthEvent.PasswordChanged -> _uiState.update { it.copy(password = event.password) }
            is AuthEvent.UsernameChanged -> _uiState.update { it.copy(username = event.username) }
            is AuthEvent.ToggleAuthMode -> _uiState.update { it.copy(isLogin = !it.isLogin) }
            is AuthEvent.ToggleRememberMe -> {
                _uiState.update { it.copy(rememberMe = !it.rememberMe) }
                viewModelScope.launch {
                    settingsRepository.setRememberMe(_uiState.value.rememberMe)
                }
            }
            AuthEvent.Submit -> submit()
            AuthEvent.ClearError -> _uiState.update { it.copy(error = null) }
            AuthEvent.GuestLogin -> {
                viewModelScope.launch {
                    settingsRepository.setGuestMode(true)
                    _uiState.update { it.copy(isLoggedIn = true, isGuest = true) }
                }
            }
            is AuthEvent.ResetPassword -> {
                 viewModelScope.launch {
                     _uiState.update { it.copy(isLoading = true, error = null) }
                     authRepository.sendPasswordResetEmail(event.email)
                         .onSuccess {
                             _uiState.update { it.copy(isLoading = false, resetPasswordSuccess = true) }
                         }
                         .onFailure { error ->
                             val msg = com.melikenurozun.recipe_app.presentation.util.UiMessageHelper.getMessage(error)
                             _uiState.update { it.copy(isLoading = false, error = msg) }
                         }
                 }
            }
            AuthEvent.ClearResetSuccess -> _uiState.update { it.copy(resetPasswordSuccess = false) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(isLoggedIn = false) } 
        }
    }

    private fun submit() {
        val state = uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all fields.") }
            return
        }

        if (!state.isLogin && state.username.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a username.") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Clean up email input to avoid validation errors
                val cleanEmail = state.email.trim()
                
                if (state.isLogin) {
                    authRepository.signIn(
                        email = cleanEmail,
                        password = state.password
                    ).getOrThrow()
                } else {
                    authRepository.signUp(
                        email = cleanEmail,
                        password = state.password,
                        username = state.username
                    ).getOrThrow()
                }
                
                // Wait briefly for session to propagate
                kotlinx.coroutines.delay(500)
                
                // Verify we actually have a session
                if (authRepository.getCurrentUserId() != null) {
                    settingsRepository.setGuestMode(false)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true, isGuest = false) }
                } else {
                    authRepository.signOut()
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Login failed. Please try again." 
                        ) 
                    }
                }
            } catch (e: Exception) {
                val errorMessage = com.melikenurozun.recipe_app.presentation.util.UiMessageHelper.getMessage(e)
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isLogin: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
    val rememberMe: Boolean = false,
    val resetPasswordSuccess: Boolean = false
)

sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class UsernameChanged(val username: String) : AuthEvent()
    object ToggleAuthMode : AuthEvent()
    object ToggleRememberMe : AuthEvent()
    object Submit : AuthEvent()
    object ClearError : AuthEvent()
    object GuestLogin : AuthEvent()
    data class ResetPassword(val email: String) : AuthEvent()
    object ClearResetSuccess : AuthEvent()
}
