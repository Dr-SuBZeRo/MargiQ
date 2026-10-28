package com.margiq.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Safe utility to launch external URLs in the user's preferred web browser.
 */
object UrlLauncher {

    const val GITHUB_REPO_URL = "https://github.com/Dr-SuBZeRo/MargiQ"
    const val GITHUB_RELEASES_URL = "https://github.com/Dr-SuBZeRo/MargiQ/releases"

    /**
     * Safely dispatches an [Intent.ACTION_VIEW] for the provided [url].
     * Returns true if successfully started, or false if no capable application is found.
     */
    fun openUrl(context: Context, url: String): Boolean {
        return try {
            val uri = Uri.parse(url.trim())
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}

