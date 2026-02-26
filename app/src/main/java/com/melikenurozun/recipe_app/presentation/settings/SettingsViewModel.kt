package com.melikenurozun.recipe_app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.data.repository.SettingsRepository
import com.melikenurozun.recipe_app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val isDarkMode = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    val current = isDarkMode.value
                    settingsRepository.setDarkMode(!current)
                }
            }
            is SettingsEvent.SignOut -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    settingsRepository.setGuestMode(false) 
                    // Note: SignOut callback is handled in UI usually, but clearing guest mode is important.
                }
            }
        }
    }
}

sealed class SettingsEvent {
    object ToggleDarkMode : SettingsEvent()
    object SignOut : SettingsEvent()
}
