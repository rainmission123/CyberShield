package com.jorian.cybershield.utils

import java.net.URI

object UrlUtils {

    fun normalize(url: String): String {
        val trimmed = url.trim()

        return when {
            trimmed.isEmpty() -> ""
            trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
    }

    fun isValidUrl(url: String): Boolean {
        val normalized = normalize(url)

        return try {
            val uri = URI(normalized)
            val scheme = uri.scheme?.lowercase()
            val host = uri.host

            (scheme == "http" || scheme == "https") &&
                    !host.isNullOrBlank() &&
                    host.contains(".")
        } catch (e: Exception) {
            false
        }
    }
}
