package com.margiq.ui.settings

import com.margiq.data.preferences.UserPreferences

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val showResetDefaultsConfirmDialog: Boolean = false,
    val userMessage: String? = null
)
