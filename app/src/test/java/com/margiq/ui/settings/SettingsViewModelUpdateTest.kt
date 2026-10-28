package com.margiq.ui.settings

import com.margiq.domain.updater.GitHubRelease
import com.margiq.domain.updater.GitHubReleaseAsset
import com.margiq.domain.updater.GitHubUpdateResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelUpdateTest {

    @Test
    fun testProcessUpdateResultNewerVersionWithApk() {
        val release = GitHubRelease(
            tagName = "v2.2.0",
            name = "MargiQ v2.2.0 Major Update",
            body = "Brand new features",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.2.0",
            isDraft = false,
            isPrerelease = false,
            assets = listOf(
                GitHubReleaseAsset(
                    name = "MargiQ-v2.2.0-release.apk",
                    sizeBytes = 14_000_000L,
                    browserDownloadUrl = "https://download/MargiQ-v2.2.0-release.apk",
                    contentType = "application/vnd.android.package-archive"
                )
            )
        )

        val state = SettingsViewModel.processUpdateResult(
            result = GitHubUpdateResult.Success(release),
            installedVersionName = "2.1.2"
        )

        assertTrue(state is UpdateCheckState.UpdateAvailable)
        val updateAvailable = state as UpdateCheckState.UpdateAvailable
        assertEquals("2.2.0", updateAvailable.latestVersion)
        assertEquals("MargiQ v2.2.0 Major Update", updateAvailable.releaseName)
        assertNotNull(updateAvailable.apkAsset)
        assertEquals("MargiQ-v2.2.0-release.apk", updateAvailable.apkAsset?.name)
    }

    @Test
    fun testProcessUpdateResultUpToDate() {
        val release = GitHubRelease(
            tagName = "v2.1.2",
            name = "MargiQ v2.1.2",
            body = "Current release",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.2",
            isDraft = false,
            isPrerelease = false,
            assets = emptyList()
        )

        val state = SettingsViewModel.processUpdateResult(
            result = GitHubUpdateResult.Success(release),
            installedVersionName = "2.1.2"
        )

        assertTrue(state is UpdateCheckState.UpToDate)
        val upToDate = state as UpdateCheckState.UpToDate
        assertEquals("2.1.2", upToDate.currentVersion)
    }

    @Test
    fun testProcessUpdateResultInstalledNewerThanRelease() {
        val release = GitHubRelease(
            tagName = "v2.1.2",
            name = "MargiQ v2.1.2",
            body = "Previous release",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.2",
            isDraft = false,
            isPrerelease = false,
            assets = emptyList()
        )

        val state = SettingsViewModel.processUpdateResult(
            result = GitHubUpdateResult.Success(release),
            installedVersionName = "2.1.3"
        )

        assertTrue("Installed version higher than release should report up-to-date", state is UpdateCheckState.UpToDate)
    }

    @Test
    fun testProcessUpdateResultUncertainVersion() {
        val release = GitHubRelease(
            tagName = "canary-build-unversioned",
            name = "Canary Build",
            body = "Unversioned build",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/canary",
            isDraft = false,
            isPrerelease = true,
            assets = emptyList()
        )

        val state = SettingsViewModel.processUpdateResult(
            result = GitHubUpdateResult.Success(release),
            installedVersionName = "2.1.3"
        )

        assertTrue(state is UpdateCheckState.UncertainVersion)
        val uncertain = state as UpdateCheckState.UncertainVersion
        assertEquals("canary-build-unversioned", uncertain.rawTag)
    }

    @Test
    fun testProcessUpdateResultHttpError() {
        val state = SettingsViewModel.processUpdateResult(
            result = GitHubUpdateResult.HttpError(404, "No published releases found on GitHub."),
            installedVersionName = "2.1.3"
        )

        assertTrue(state is UpdateCheckState.Error)
        val error = state as UpdateCheckState.Error
        assertEquals("No published releases found on GitHub.", error.message)
        assertEquals(false, error.isOffline)
    }

    @Test
    fun testProcessUpdateResultOfflineError() {
        val state = SettingsViewModel.processUpdateResult(
            result = GitHubUpdateResult.NetworkError(isOffline = true, message = "Unable to connect. Please check your internet connection."),
            installedVersionName = "2.1.3"
        )

        assertTrue(state is UpdateCheckState.Error)
        val error = state as UpdateCheckState.Error
        assertEquals(true, error.isOffline)
    }
}

