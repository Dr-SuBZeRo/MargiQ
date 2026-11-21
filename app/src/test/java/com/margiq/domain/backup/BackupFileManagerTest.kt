package com.margiq.domain.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BackupFileManagerTest {

    @Test
    fun generateSuggestedExportFilename_containsPrefixAndTimestamp() {
        val filename = BackupFileManager.generateSuggestedExportFilename(1732180000000L)
        assertTrue(filename.startsWith(BackupConstants.SUGGESTED_FILE_PREFIX))
        assertTrue(filename.endsWith(BackupConstants.DEFAULT_FILE_EXTENSION))
    }

    @Test
    fun writeAndReadStream_roundTrip_preservesContentExactly() {
        val originalText = """{"format":"com.margiq.calculation-backup","schemaVersion":1,"records":[]}"""
        val outStream = ByteArrayOutputStream()
        BackupFileManager.writeStringToStream(outStream, originalText)

        val inStream = ByteArrayInputStream(outStream.toByteArray())
        val recoveredText = BackupFileManager.readStreamWithLimit(inStream)

        assertEquals(originalText, recoveredText)
    }

    @Test
    fun writeAndReadStream_preservesUnicodeAndSpecialCharacters() {
        val originalText = "MargiQ Backup • Paracetamol ₹650 / மெட்டீரியல் / 특수문자 <test & quotes \"100%\">"
        val outStream = ByteArrayOutputStream()
        BackupFileManager.writeStringToStream(outStream, originalText)

        val inStream = ByteArrayInputStream(outStream.toByteArray())
        val recoveredText = BackupFileManager.readStreamWithLimit(inStream)

        assertEquals(originalText, recoveredText)
    }

    @Test
    fun readStreamWithLimit_emptyStream_returnsEmptyString() {
        val inStream = ByteArrayInputStream(ByteArray(0))
        val recoveredText = BackupFileManager.readStreamWithLimit(inStream)
        assertEquals("", recoveredText)
    }

    @Test
    fun readStreamWithLimit_exceedingLimit_throwsIllegalArgumentException() {
        val largeData = "A".repeat(1024)
        val inStream = ByteArrayInputStream(largeData.toByteArray(Charsets.UTF_8))

        try {
            BackupFileManager.readStreamWithLimit(inStream, maxSizeBytes = 500)
            fail("Expected IllegalArgumentException for exceeding size limit")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("limit") == true)
        }
    }
}
