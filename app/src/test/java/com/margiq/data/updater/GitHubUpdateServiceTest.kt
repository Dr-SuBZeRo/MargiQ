package com.margiq.data.updater

import com.margiq.domain.updater.GitHubUpdateResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdateServiceTest {

    @Test
    fun testParseValidReleaseJson() {
        val json = """
            {
              "tag_name": "v2.1.3",
              "name": "MargiQ v2.1.3 — Version Display & Update Checker",
              "body": "Detailed release description",
              "html_url": "https://github.com/Dr-SuBZeRo/MargiQ/releases/tag/v2.1.3",
              "draft": false,
              "prerelease": false,
              "assets": [
                {
                  "name": "MargiQ-v2.1.3-release.apk",
                  "size": 14000000,
                  "browser_download_url": "https://github.com/Dr-SuBZeRo/MargiQ/releases/download/v2.1.3/MargiQ-v2.1.3-release.apk",
                  "content_type": "application/vnd.android.package-archive"
                }
              ]
            }
        """.trimIndent()

        val result = DefaultGitHubUpdateService.parseReleaseJson(json)
        assertTrue(result is GitHubUpdateResult.Success)
        val success = result as GitHubUpdateResult.Success
        assertEquals("v2.1.3", success.release.tagName)
        assertEquals("MargiQ v2.1.3 — Version Display & Update Checker", success.release.name)
        assertEquals("Detailed release description", success.release.body)
        assertFalse(success.release.isDraft)
        assertEquals(1, success.release.assets.size)
        assertEquals("MargiQ-v2.1.3-release.apk", success.release.assets[0].name)
        assertEquals(14000000L, success.release.assets[0].sizeBytes)
    }

    @Test
    fun testParseMissingTagName() {
        val json = """
            {
              "name": "Missing tag release",
              "body": "No tag specified"
            }
        """.trimIndent()

        val result = DefaultGitHubUpdateService.parseReleaseJson(json)
        assertTrue(result is GitHubUpdateResult.ParseError)
    }

    @Test
    fun testParseCorruptJson() {
        val json = "NOT_A_JSON_STRING"
        val result = DefaultGitHubUpdateService.parseReleaseJson(json)
        assertTrue(result is GitHubUpdateResult.ParseError)
    }
}

