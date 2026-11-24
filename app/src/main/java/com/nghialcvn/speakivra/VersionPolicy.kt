package com.nghialcvn.speakivra

/** One decimal digit per minor and patch slot. 1.0.9 -> 1.1.0. */
internal object VersionPolicy {
    private val VERSION = Regex("""(0|[1-9][0-9]*)\.[0-9]\.[0-9]""")
    fun next(version: String): String {
        require(VERSION.matches(version)) { "Unsupported Speakivra version: $version" }
        val (major, minor, patch) = version.split('.').map(String::toInt)
        return when {
            patch < 9 -> "$major.$minor.${patch + 1}"
            minor < 9 -> "$major.${minor + 1}.0"
            else -> "${major + 1}.0.0"
        }
    }
    fun valid(version: String): Boolean = VERSION.matches(version)
}
