package com.jorian.cybershield.data

import android.content.Context
import org.json.JSONArray

class UrlScanRepository(
    context: Context
) {

    private val prefs = context.getSharedPreferences(
        "url_scan_repository",
        Context.MODE_PRIVATE
    )

    fun saveScannedUrl(url: String) {
        val normalized = url.trim()
        if (normalized.isEmpty()) return

        val current = getScannedUrls().toMutableList()
        current.remove(normalized)
        current.add(0, normalized)

        val jsonArray = JSONArray()
        current.take(MAX_URLS).forEach { jsonArray.put(it) }

        prefs.edit().putString(KEY_URLS, jsonArray.toString()).apply()
    }

    fun getScannedUrls(): List<String> {
        return try {
            val jsonArray = JSONArray(prefs.getString(KEY_URLS, "[]") ?: "[]")
            buildList {
                for (i in 0 until jsonArray.length()) {
                    add(jsonArray.optString(i))
                }
            }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val KEY_URLS = "urls"
        private const val MAX_URLS = 50
    }
}
