package com.margiq.ui.sharing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import com.margiq.domain.sharing.EmailContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareCoordinator {

    /**
     * Shares formatted plain text via the standard Android Sharesheet.
     */
    fun shareText(context: Context, text: String, chooserTitle: String = "Share Report") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Copies plain text to the system clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "MargiQ Report") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Caches a rendered bitmap to private cache and shares it securely using [FileProvider].
     */
    suspend fun shareImage(
        context: Context,
        bitmap: Bitmap,
        title: String = "MargiQ Report"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "share_images").apply { mkdirs() }
            val cacheFile = File(cacheDir, "MargiQ_Share_${System.currentTimeMillis()}.png")

            FileOutputStream(cacheFile).use { stream ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                if (!compressed) throw Exception("Failed to compress bitmap")
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Summary Card").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(chooser)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Prepares an email draft with HTML formatting and plain-text fallback.
     */
    fun shareHtmlEmail(
        context: Context,
        content: EmailContent,
        chooserTitle: String = "Send Report via Email"
    ) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, content.subject)
            putExtra(
                Intent.EXTRA_TEXT,
                HtmlCompat.fromHtml(content.htmlBody, HtmlCompat.FROM_HTML_MODE_LEGACY)
            )
            putExtra(Intent.EXTRA_HTML_TEXT, content.htmlBody)
        }

        val chooser = Intent.createChooser(emailIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

