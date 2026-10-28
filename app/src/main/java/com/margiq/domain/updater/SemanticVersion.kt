package com.margiq.domain.updater

/**
 * Robust semantic version representation supporting normal major.minor.patch
 * versions, leading 'v' normalization, and optional pre-release identifiers.
 */
data class SemanticVersion(
    val major: Int,
    val minor: Int = 0,
    val patch: Int = 0,
    val preRelease: String? = null
) : Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        if (this.major != other.major) return this.major.compareTo(other.major)
        if (this.minor != other.minor) return this.minor.compareTo(other.minor)
        if (this.patch != other.patch) return this.patch.compareTo(other.patch)

        // SemVer 2.0.0 specification rule 11:
        // A normal version without a pre-release suffix has higher precedence than one with a pre-release suffix.
        return when {
            this.preRelease == null && other.preRelease != null -> 1
            this.preRelease != null && other.preRelease == null -> -1
            this.preRelease != null && other.preRelease != null -> this.preRelease.compareTo(other.preRelease)
            else -> 0
        }
    }

    val normalizedString: String
        get() = buildString {
            append("$major.$minor.$patch")
            if (!preRelease.isNullOrBlank()) {
                append("-$preRelease")
            }
        }

    companion object {
        // Matches e.g. "v2.1.2", "2.1.2", "2.0", "v2.1.3-beta.1", "V3.0.0"
        private val SEMVER_REGEX = Regex(
            """^[vV]?(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-([0-9A-Za-z.-]+))?"""
        )

        /**
         * Parses a version string into a [SemanticVersion].
         * Returns `null` if the string cannot be parsed reliably into semantic numbers.
         */
        fun parse(versionString: String?): SemanticVersion? {
            if (versionString.isNullOrBlank()) return null
            val match = SEMVER_REGEX.find(versionString.trim()) ?: return null
            val major = match.groupValues[1].toIntOrNull() ?: return null
            val minor = match.groupValues[2].takeIf { it.isNotEmpty() }?.toIntOrNull() ?: 0
            val patch = match.groupValues[3].takeIf { it.isNotEmpty() }?.toIntOrNull() ?: 0
            val preRelease = match.groupValues[4].takeIf { it.isNotEmpty() }
            return SemanticVersion(major, minor, patch, preRelease)
        }
    }
}

