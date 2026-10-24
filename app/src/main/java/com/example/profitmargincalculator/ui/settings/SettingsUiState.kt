package com.example.profitmargincalculator.ui.settings

import com.example.profitmargincalculator.data.preferences.UserPreferences

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val showResetDefaultsConfirmDialog: Boolean = false,
    val userMessage: String? = null
)
