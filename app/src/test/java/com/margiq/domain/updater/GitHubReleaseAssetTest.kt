package com.margiq.domain.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseAssetTest {

    @Test
    fun testSingleApkResolution() {
        val release = GitHubRelease(
            tagName = "v2.1.3",
            name = "MargiQ v2.1.3",
            body = "Release notes",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.3",
            isDraft = false,
            isPrerelease = false,
            assets = listOf(
                GitHubReleaseAsset(
                    name = "MargiQ.apk",
                    sizeBytes = 10_000_000L,
                    browserDownloadUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/download/v2.1.3/MargiQ.apk",
                    contentType = "application/vnd.android.package-archive"
                )
            )
        )

        val result = release.resolveApkAsset()
        assertTrue(result is ApkAssetSelection.Selected)
        assertEquals("MargiQ.apk", (result as ApkAssetSelection.Selected).asset.name)
    }

    @Test
    fun testProductionReleaseApkResolutionOverDebug() {
        val release = GitHubRelease(
            tagName = "v2.1.3",
            name = "MargiQ v2.1.3",
            body = "Release notes",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.3",
            isDraft = false,
            isPrerelease = false,
            assets = listOf(
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-release.apk",
                    sizeBytes = 14_000_000L,
                    browserDownloadUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/download/v2.1.3/MargiQ-v2.1.3-release.apk",
                    contentType = "application/vnd.android.package-archive"
                ),
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-release.aab",
                    sizeBytes = 13_500_000L,
                    browserDownloadUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/download/v2.1.3/MargiQ-v2.1.3-release.aab",
                    contentType = "application/octet-stream"
                ),
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-debug.apk",
                    sizeBytes = 21_000_000L,
                    browserDownloadUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/download/v2.1.3/MargiQ-v2.1.3-debug.apk",
                    contentType = "application/vnd.android.package-archive"
                )
            )
        )

        val result = release.resolveApkAsset()
        assertTrue(result is ApkAssetSelection.Selected)
        assertEquals("MargiQ-v2.1.3-release.apk", (result as ApkAssetSelection.Selected).asset.name)
    }

    @Test
    fun testUniversalApkPreferredOverArchApks() {
        val release = GitHubRelease(
            tagName = "v2.1.3",
            name = "MargiQ v2.1.3",
            body = "Release notes",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.3",
            isDraft = false,
            isPrerelease = false,
            assets = listOf(
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-arm64-v8a.apk",
                    sizeBytes = 12_000_000L,
                    browserDownloadUrl = "https://download/arm64.apk",
                    contentType = "application/vnd.android.package-archive"
                ),
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-universal.apk",
                    sizeBytes = 18_000_000L,
                    browserDownloadUrl = "https://download/universal.apk",
                    contentType = "application/vnd.android.package-archive"
                )
            )
        )

        val result = release.resolveApkAsset()
        assertTrue(result is ApkAssetSelection.Selected)
        assertEquals("MargiQ-v2.1.3-universal.apk", (result as ApkAssetSelection.Selected).asset.name)
    }

    @Test
    fun testAmbiguousMultipleApks() {
        val release = GitHubRelease(
            tagName = "v2.1.3",
            name = "MargiQ v2.1.3",
            body = "Release notes",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.3",
            isDraft = false,
            isPrerelease = false,
            assets = listOf(
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-arm64.apk",
                    sizeBytes = 12_000_000L,
                    browserDownloadUrl = "https://download/arm64.apk",
                    contentType = "application/vnd.android.package-archive"
                ),
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-x86_64.apk",
                    sizeBytes = 13_000_000L,
                    browserDownloadUrl = "https://download/x86.apk",
                    contentType = "application/vnd.android.package-archive"
                )
            )
        )

        val result = release.resolveApkAsset()
        assertEquals(ApkAssetSelection.Ambiguous, result)
    }

    @Test
    fun testNoApkAssets() {
        val release = GitHubRelease(
            tagName = "v2.1.3",
            name = "MargiQ v2.1.3",
            body = "Release notes",
            htmlUrl = "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.3",
            isDraft = false,
            isPrerelease = false,
            assets = listOf(
                GitHubReleaseAsset(
                    name = "MargiQ-v2.1.3-release.aab",
                    sizeBytes = 13_500_000L,
                    browserDownloadUrl = "https://download/release.aab",
                    contentType = "application/octet-stream"
                )
            )
        )

        val result = release.resolveApkAsset()
        assertEquals(ApkAssetSelection.None, result)
    }
}

