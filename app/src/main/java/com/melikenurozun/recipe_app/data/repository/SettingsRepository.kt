package com.melikenurozun.recipe_app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val REMEMBER_ME = booleanPreferencesKey("remember_me")

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    val rememberMe: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[REMEMBER_ME] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    suspend fun setRememberMe(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_ME] = enabled
        }
    }

    private val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")

    val isGuestMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_GUEST_MODE] ?: false
    }

    suspend fun setGuestMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_GUEST_MODE] = enabled
        }
    }
}
