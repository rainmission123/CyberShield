package com.jorian.cybershield

object UrlSafetyManager {

    data class ScanResult(
        val isSafe: Boolean,
        val score: Int,
        val reason: String
    )

    fun scanUrl(url: String): ScanResult {
        val lowerUrl = url.lowercase()

        val dangerousWords = listOf(
            "login", "verify", "free", "bonus", "claim",
            "password", "bank", "wallet", "crypto",
            "gift", "prize", "urgent", "update-account"
        )

        val suspiciousDomains = listOf(
            "bit.ly", "tinyurl", "is.gd", "grabify",
            "free-gift", "verify-login", "account-secure"
        )

        var score = 0

        dangerousWords.forEach {
            if (lowerUrl.contains(it)) score += 10
        }

        suspiciousDomains.forEach {
            if (lowerUrl.contains(it)) score += 20
        }

        if (!lowerUrl.startsWith("https://")) score += 15
        if (lowerUrl.length > 100) score += 10
        if (lowerUrl.count { it == '-' } >= 3) score += 10

        return when {
            score >= 40 -> ScanResult(false, score, "Dangerous link detected")
            score >= 20 -> ScanResult(false, score, "Suspicious phishing link")
            else -> ScanResult(true, score, "Safe link")
        }
    }
}