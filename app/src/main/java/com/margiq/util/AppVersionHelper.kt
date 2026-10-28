package com.margiq.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.margiq.BuildConfig

/**
 * Encapsulates the application's installed version information.
 */
data class AppVersionInfo(
    val versionName: String,
    val versionCode: Long
) {
    /**
     * Standard compact display string, e.g. "v2.1.3".
     */
    val displayVersion: String
        get() = "v$versionName"

    /**
     * Informative display string, e.g. "Version 2.1.3 (Build 11)".
     */
    val fullDisplayVersion: String
        get() = "Version $versionName (Build $versionCode)"
}

/**
 * Single authoritative helper for inspecting application package metadata.
 * Dynamically queries the installed [PackageInfo] via [PackageManager] across all
 * supported Android API levels (API 26 to 36), safely falling back to [BuildConfig]
 * if the package metadata is temporarily unavailable.
 */
object AppVersionHelper {

    fun getAppVersionInfo(context: Context): AppVersionInfo {
        return try {
            val packageInfo = getPackageInfo(context)
            fromPackageInfo(packageInfo)
        } catch (_: Exception) {
            fromBuildConfig()
        }
    }

    fun getPackageInfo(context: Context): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }

    fun fromPackageInfo(packageInfo: PackageInfo): AppVersionInfo {
        val name = packageInfo.versionName?.takeIf { it.isNotBlank() } ?: BuildConfig.VERSION_NAME
        val code = PackageInfoCompat.getLongVersionCode(packageInfo)
        return AppVersionInfo(versionName = name, versionCode = code)
    }

    fun fromBuildConfig(): AppVersionInfo {
        return AppVersionInfo(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE.toLong()
        )
    }
}

