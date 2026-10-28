package com.margiq.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionHelperTest {

    @Test
    fun testAppVersionInfoFormatting() {
        val info = AppVersionInfo(versionName = "2.1.3", versionCode = 11L)
        assertEquals("v2.1.3", info.displayVersion)
        assertEquals("Version 2.1.3 (Build 11)", info.fullDisplayVersion)
    }

    @Test
    fun testFromBuildConfigFallback() {
        val info = AppVersionHelper.fromBuildConfig()
        assertTrue("versionName should not be empty", info.versionName.isNotEmpty())
        assertTrue("versionCode should be positive", info.versionCode > 0)
        assertEquals("v${info.versionName}", info.displayVersion)
    }
}

