package com.margiq.domain.updater

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVersionTest {

    @Test
    fun testNormalVersionComparison() {
        val v211 = SemanticVersion.parse("2.1.1")!!
        val v212 = SemanticVersion.parse("2.1.2")!!
        val v219 = SemanticVersion.parse("2.1.9")!!
        val v220 = SemanticVersion.parse("2.2.0")!!
        val v290 = SemanticVersion.parse("2.9.0")!!
        val v2100 = SemanticVersion.parse("2.10.0")!!
        val v300 = SemanticVersion.parse("3.0.0")!!

        assertTrue("2.1.2 should be greater than 2.1.1", v212 > v211)
        assertTrue("2.2.0 should be greater than 2.1.9", v220 > v219)
        assertTrue("2.10.0 should be greater than 2.9.0", v2100 > v290)
        assertTrue("3.0.0 should be greater than 2.10.0", v300 > v2100)
    }

    @Test
    fun testEqualVersionsWithAndWithoutVPrefix() {
        val v1 = SemanticVersion.parse("v2.1.2")!!
        val v2 = SemanticVersion.parse("2.1.2")!!
        val v3 = SemanticVersion.parse("V2.1.2")!!

        assertEquals(0, v1.compareTo(v2))
        assertEquals(0, v2.compareTo(v3))
        assertEquals("2.1.2", v1.normalizedString)
        assertEquals("2.1.2", v2.normalizedString)
    }

    @Test
    fun testPreReleaseHandling() {
        val release = SemanticVersion.parse("2.1.3")!!
        val preRelease = SemanticVersion.parse("2.1.3-beta")!!
        val preRelease1 = SemanticVersion.parse("v2.1.3-beta.1")!!
        val preRelease2 = SemanticVersion.parse("v2.1.3-beta.2")!!

        assertTrue("Full release should have higher precedence than pre-release", release > preRelease)
        assertTrue("beta.2 should have higher precedence than beta.1", preRelease2 > preRelease1)
    }

    @Test
    fun testPartialVersions() {
        val vMajor = SemanticVersion.parse("2")!!
        val vMajorMinor = SemanticVersion.parse("2.1")!!
        val vFull = SemanticVersion.parse("2.1.0")!!

        assertEquals(2, vMajor.major)
        assertEquals(0, vMajor.minor)
        assertEquals(0, vMajor.patch)

        assertEquals(2, vMajorMinor.major)
        assertEquals(1, vMajorMinor.minor)
        assertEquals(0, vMajorMinor.patch)

        assertEquals(0, vMajorMinor.compareTo(vFull))
    }

    @Test
    fun testMalformedAndNullTags() {
        assertNull(SemanticVersion.parse(null))
        assertNull(SemanticVersion.parse(""))
        assertNull(SemanticVersion.parse("   "))
        assertNull(SemanticVersion.parse("nightly-build"))
        assertNull(SemanticVersion.parse("experimental"))
        assertNotNull(SemanticVersion.parse("v2.1.3"))
    }
}

