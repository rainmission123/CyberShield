package com.jorian.cybershield.data

import android.content.Context
import com.jorian.cybershield.model.HistoryItem
import com.jorian.cybershield.model.ScanStatus
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("scan_history", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS = "items"
        private const val MAX_HISTORY = 50
    }

    fun saveScan(url: String, status: ScanStatus, score: Int) {
        val history = getHistory().toMutableList()

        val latest = history.firstOrNull()
        if (latest != null &&
            latest.url == url &&
            latest.status == status &&
            latest.score == score
        ) {
            return
        }

        val item = HistoryItem(
            url = url,
            status = status,
            score = score,
            dateTime = getCurrentDateTime()
        )

        history.add(0, item)
        saveHistory(history.take(MAX_HISTORY))
    }

    fun getHistory(): List<HistoryItem> {
        return try {
            val json = prefs.getString(KEY_ITEMS, "[]") ?: "[]"
            val jsonArray = JSONArray(json)
            val list = mutableListOf<HistoryItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                list.add(
                    HistoryItem(
                        url = obj.optString("url", ""),
                        status = runCatching {
                            ScanStatus.valueOf(obj.optString("status", ScanStatus.SAFE.name))
                        }.getOrDefault(ScanStatus.SAFE),
                        score = obj.optInt("score", 0),
                        dateTime = obj.optString("dateTime", "")
                    )
                )
            }

            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteItemAt(position: Int) {
        val history = getHistory().toMutableList()

        if (position in history.indices) {
            history.removeAt(position)
        }

        saveHistory(history)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_ITEMS).apply()
    }

    private fun saveHistory(history: List<HistoryItem>) {
        val jsonArray = JSONArray()

        history.forEach {
            val obj = JSONObject().apply {
                put("url", it.url)
                put("status", it.status.name)
                put("score", it.score)
                put("dateTime", it.dateTime)
            }

            jsonArray.put(obj)
        }

        prefs.edit()
            .putString(KEY_ITEMS, jsonArray.toString())
            .apply()
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "MMM dd, yyyy • hh:mm a",
            Locale.getDefault()
        ).format(Date())
    }
}