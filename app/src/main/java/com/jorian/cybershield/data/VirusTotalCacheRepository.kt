package com.jorian.cybershield.data

import android.content.Context
import com.jorian.cybershield.domain.VirusTotalScanner
import org.json.JSONObject

class VirusTotalCacheRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences(
        "virus_total_cache",
        Context.MODE_PRIVATE
    )

    private val cacheDurationMillis = 24 * 60 * 60 * 1000L // 24 hours

    fun getCachedResult(url: String): VirusTotalScanner.VirusTotalResult? {
        val key = makeKey(url)
        val json = prefs.getString(key, null) ?: return null

        return try {
            val obj = JSONObject(json)
            val savedAt = obj.getLong("savedAt")
            val now = System.currentTimeMillis()

            if (now - savedAt > cacheDurationMillis) {
                prefs.edit().remove(key).apply()
                return null
            }

            VirusTotalScanner.VirusTotalResult(
                success = obj.getBoolean("success"),
                malicious = obj.getInt("malicious"),
                suspicious = obj.getInt("suspicious"),
                harmless = obj.getInt("harmless"),
                undetected = obj.getInt("undetected"),
                message = obj.getString("message")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveResult(
        url: String,
        result: VirusTotalScanner.VirusTotalResult
    ) {
        val key = makeKey(url)

        val obj = JSONObject().apply {
            put("success", result.success)
            put("malicious", result.malicious)
            put("suspicious", result.suspicious)
            put("harmless", result.harmless)
            put("undetected", result.undetected)
            put("message", result.message)
            put("savedAt", System.currentTimeMillis())
        }

        prefs.edit()
            .putString(key, obj.toString())
            .apply()
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }

    private fun makeKey(url: String): String {
        return url.trim()
            .lowercase()
            .replace(".", "_")
            .replace("/", "_")
            .replace(":", "_")
            .replace("?", "_")
            .replace("&", "_")
            .replace("=", "_")
    }
}