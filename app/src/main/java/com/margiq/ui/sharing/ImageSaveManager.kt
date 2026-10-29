package com.margiq.ui.sharing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageSaveManager {

    /**
     * Saves a generated summary report bitmap to the public "Pictures/MargiQ" directory.
     * Uses Scoped Storage MediaStore on API 29+ with IS_PENDING safety,
     * and legacy direct directory writing with media scanner on API 26-28.
     *
     * Returns [Result.success] with the human-readable saved file description,
     * or [Result.failure] on error.
     */
    suspend fun saveImageToPictures(
        context: Context,
        bitmap: Bitmap,
        reportPrefix: String = "Report"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "MargiQ_${reportPrefix}_$timestamp.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MargiQ")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val imageUri: Uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to allocate MediaStore entry"))

                try {
                    resolver.openOutputStream(imageUri)?.use { outputStream ->
                        val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        if (!compressed) throw Exception("Bitmap compression failed")
                    } ?: throw Exception("Failed to open output stream for MediaStore")

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)

                    Result.success("Saved to Pictures/MargiQ/$fileName")
                } catch (e: Exception) {
                    // Clean up partial pending entry on failure
                    try { resolver.delete(imageUri, null, null) } catch (_: Exception) {}
                    Result.failure(e)
                }
            } else {
                // API 26-28 fallback
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val margiqDir = File(picturesDir, "MargiQ").apply { mkdirs() }
                val targetFile = File(margiqDir, fileName)

                FileOutputStream(targetFile).use { stream ->
                    val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    if (!compressed) throw Exception("Bitmap compression failed")
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf("image/png"),
                    null
                )

                Result.success("Saved to Pictures/MargiQ/$fileName")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

