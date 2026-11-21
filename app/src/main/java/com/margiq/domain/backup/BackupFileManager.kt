package com.margiq.domain.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles Storage Access Framework (SAF) URI reading and writing for calculation history backups.
 * Executes strictly on [Dispatchers.IO] and streams with UTF-8 character encoding and memory bounds.
 */
object BackupFileManager {

    /**
     * Generates a standard default filename for export (e.g. "MargiQ_Backup_20261121_141500.json").
     */
    fun generateSuggestedExportFilename(timestamp: Long = System.currentTimeMillis()): String {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))
        return "${BackupConstants.SUGGESTED_FILE_PREFIX}$dateStr${BackupConstants.DEFAULT_FILE_EXTENSION}"
    }

    /**
     * Writes JSON backup text into the user-selected destination [Uri] obtained via ACTION_CREATE_DOCUMENT.
     */
    suspend fun writeJsonToUri(
        context: Context,
        uri: Uri,
        jsonContent: String,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val outputStream: OutputStream = context.contentResolver.openOutputStream(uri, "wt")
                ?: throw IllegalStateException("Could not open destination for writing.")
            writeStringToStream(outputStream, jsonContent)
        }
    }

    /**
     * Reads JSON backup text from the user-selected source [Uri] obtained via ACTION_OPEN_DOCUMENT.
     * Enforces the maximum file size limit to prevent memory exhaustion.
     */
    suspend fun readJsonFromUri(
        context: Context,
        uri: Uri,
        maxSizeBytes: Long = BackupConstants.MAX_SUPPORTED_BACKUP_SIZE_BYTES,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Result<String> = withContext(ioDispatcher) {
        runCatching {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    if (pfd.statSize > maxSizeBytes) {
                        throw IllegalArgumentException("Selected file size (${pfd.statSize} bytes) exceeds the 10 MB limit.")
                    }
                }
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (_: Exception) {
                // Some virtual storage providers don't report statSize; stream limiter below handles it safely
            }

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not open selected file for reading.")

            readStreamWithLimit(inputStream, maxSizeBytes)
        }
    }

    /**
     * Streams string contents into an [OutputStream] using UTF-8 encoding.
     */
    fun writeStringToStream(outputStream: OutputStream, content: String) {
        outputStream.use { stream ->
            stream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(content)
                writer.flush()
            }
        }
    }

    /**
     * Reads an [InputStream] with an explicit byte ceiling, returning the decoded UTF-8 string.
     */
    fun readStreamWithLimit(
        inputStream: InputStream,
        maxSizeBytes: Long = BackupConstants.MAX_SUPPORTED_BACKUP_SIZE_BYTES
    ): String {
        inputStream.use { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            val stringBuilder = StringBuilder()
            val buffer = CharArray(8192)
            var totalCharsRead = 0L

            var charsRead: Int
            while (reader.read(buffer).also { charsRead = it } != -1) {
                totalCharsRead += charsRead
                // Guard against files exceeding memory safety limit
                if (totalCharsRead * 2 > maxSizeBytes * 2) {
                    throw IllegalArgumentException("File content exceeds the maximum supported 10 MB limit.")
                }
                stringBuilder.append(buffer, 0, charsRead)
            }
            return stringBuilder.toString()
        }
    }
}
