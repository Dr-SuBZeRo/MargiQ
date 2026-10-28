package com.margiq.domain.updater

/**
 * Representation of a published asset attached to a GitHub release.
 */
data class GitHubReleaseAsset(
    val name: String,
    val sizeBytes: Long,
    val browserDownloadUrl: String,
    val contentType: String
) {
    val isApk: Boolean
        get() = name.endsWith(".apk", ignoreCase = true) ||
                contentType.equals("application/vnd.android.package-archive", ignoreCase = true)
}

/**
 * Result of evaluating release assets for a compatible APK download link.
 */
sealed class ApkAssetSelection {
    /** An unambiguous compatible APK asset was resolved. */
    data class Selected(val asset: GitHubReleaseAsset) : ApkAssetSelection()

    /** Multiple APK assets exist, but a unique compatible APK cannot be determined automatically. */
    data object Ambiguous : ApkAssetSelection()

    /** The release has no APK assets attached. */
    data object None : ApkAssetSelection()
}

/**
 * Representation of a published GitHub release.
 */
data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val isDraft: Boolean,
    val isPrerelease: Boolean,
    val assets: List<GitHubReleaseAsset>
) {
    /**
     * Resolves an APK asset according to the project's download selection policy:
     * 1. Filters for APK assets.
     * 2. If none exist -> [ApkAssetSelection.None].
     * 3. If exactly one exists -> [ApkAssetSelection.Selected].
     * 4. If an explicitly named "universal" APK exists -> [ApkAssetSelection.Selected].
     * 5. If a single production "release" APK exists (excluding debug) -> [ApkAssetSelection.Selected].
     * 6. Otherwise -> [ApkAssetSelection.Ambiguous] (directing user to choose from release page).
     */
    fun resolveApkAsset(): ApkAssetSelection {
        val apkAssets = assets.filter { it.isApk }
        if (apkAssets.isEmpty()) {
            return ApkAssetSelection.None
        }
        if (apkAssets.size == 1) {
            return ApkAssetSelection.Selected(apkAssets.first())
        }

        // 1. Prefer explicitly named universal APK
        val universalApks = apkAssets.filter { it.name.contains("universal", ignoreCase = true) }
        if (universalApks.size == 1) {
            return ApkAssetSelection.Selected(universalApks.first())
        }

        // 2. Prefer release APK excluding debug builds
        val releaseApks = apkAssets.filter {
            it.name.contains("release", ignoreCase = true) &&
            !it.name.contains("debug", ignoreCase = true)
        }
        if (releaseApks.size == 1) {
            return ApkAssetSelection.Selected(releaseApks.first())
        }

        // Ambiguous choice among multiple candidates
        return ApkAssetSelection.Ambiguous
    }
}

