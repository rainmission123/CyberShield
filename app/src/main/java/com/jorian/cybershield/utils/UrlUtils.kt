package com.jorian.cybershield.utils

object UrlUtils {

    fun normalize(url: String): String {
        return url.trim()
    }

    fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") ||
                url.startsWith("https://")
    }
}