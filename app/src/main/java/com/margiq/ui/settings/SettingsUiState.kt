package com.margiq.ui.settings

import com.margiq.data.preferences.UserPreferences
import com.margiq.domain.updater.GitHubReleaseAsset

/**
 * UI state for the manual GitHub update checker.
 */
sealed class UpdateCheckState {
    data object Idle : UpdateCheckState()
    data object Checking : UpdateCheckState()

    data class UpdateAvailable(
        val latestVersion: String,
        val releaseName: String,
        val releaseNotes: String,
        val releasePageUrl: String,
        val apkAsset: GitHubReleaseAsset?,
        val isAmbiguousApk: Boolean
    ) : UpdateCheckState()

    data class UpToDate(
        val currentVersion: String,
        val releasePageUrl: String
    ) : UpdateCheckState()

    data class UncertainVersion(
        val rawTag: String,
        val releaseName: String,
        val releasePageUrl: String
    ) : UpdateCheckState()

    data class Error(
        val message: String,
        val isOffline: Boolean
    ) : UpdateCheckState()
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val showResetDefaultsConfirmDialog: Boolean = false,
    val userMessage: String? = null,
    val updateCheckState: UpdateCheckState = UpdateCheckState.Idle
)
