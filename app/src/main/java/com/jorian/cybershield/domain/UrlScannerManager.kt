package com.jorian.cybershield.domain

import com.jorian.cybershield.model.ScanResult
import com.jorian.cybershield.model.ScanStatus
import com.jorian.cybershield.utils.UrlUtils
import java.net.URI

class UrlScannerManager {

    fun scan(url: String): ScanResult {
        val cleanUrl = normalizeUrl(url)

        if (cleanUrl.isEmpty()) {
            return ScanResult(
                url = cleanUrl,
                status = ScanStatus.DANGEROUS,
                message = "No URL provided.",
                reasons = listOf("Please enter a valid link."),
                score = 100
            )
        }

        val reasons = mutableListOf<String>()
        var score = 0

        val lowerUrl = cleanUrl.lowercase()
        val host = getHost(cleanUrl)

        if (!UrlUtils.isValidUrl(cleanUrl) || host.isBlank()) {
            return ScanResult(
                url = cleanUrl,
                status = ScanStatus.DANGEROUS,
                message = "Invalid or malformed URL.",
                reasons = listOf("CyberShield could not identify a valid website domain."),
                score = 100
            )
        }

        val ruleReasons = SuspiciousRules.check(cleanUrl)
        if (ruleReasons.isNotEmpty()) {
            score += ruleReasons.size * 8
            reasons.addAll(ruleReasons)
        }

        if (!cleanUrl.startsWith("https://")) {
            score += 15
            reasons.add("Website is not using HTTPS.")
        }

        val fakeLoginResult = FakeLoginDetector.detect(cleanUrl, host)
        score += fakeLoginResult.score
        reasons.addAll(fakeLoginResult.reasons)

        val suspiciousKeywords = listOf(
            "login", "verify", "account", "password", "claim",
            "reward", "free", "bonus", "gift", "prize",
            "wallet", "bank", "security", "update"
        )

        suspiciousKeywords.forEach { keyword ->
            if (lowerUrl.contains(keyword)) {
                score += 8
                reasons.add("Suspicious keyword detected: $keyword")
            }
        }

        val shorteners = listOf(
            "bit.ly", "tinyurl.com", "t.co", "is.gd",
            "cutt.ly", "rebrand.ly", "shorturl.at"
        )

        shorteners.forEach { shortener ->
            if (host.contains(shortener)) {
                score += 25
                reasons.add("Shortened URL detected.")
            }
        }

        val riskyTlds = listOf(
            ".xyz", ".top", ".click", ".work", ".monster",
            ".country", ".zip", ".mov", ".cam", ".tk"
        )

        riskyTlds.forEach { tld ->
            if (host.endsWith(tld)) {
                score += 20
                reasons.add("Risky domain extension detected: $tld")
            }
        }

        if (cleanUrl.length > 100) {
            score += 10
            reasons.add("URL is unusually long.")
        }

        if (host.count { it == '-' } >= 2) {
            score += 15
            reasons.add("Domain contains many hyphens.")
        }

        if (lowerUrl.contains("@")) {
            score += 25
            reasons.add("URL contains @ symbol, often used to hide real destination.")
        }

        if (Regex("""\d{1,3}(\.\d{1,3}){3}""").containsMatchIn(host)) {
            score += 30
            reasons.add("URL uses an IP address instead of a normal domain.")
        }

        val finalScore = score.coerceIn(0, 100)

        return when {
            finalScore >= 60 -> ScanResult(
                url = cleanUrl,
                status = ScanStatus.DANGEROUS,
                message = "Dangerous phishing or fake login link detected!",
                reasons = reasons.distinct(),
                score = finalScore
            )

            finalScore >= 25 -> ScanResult(
                url = cleanUrl,
                status = ScanStatus.SUSPICIOUS,
                message = "Suspicious link detected.",
                reasons = reasons.distinct(),
                score = finalScore
            )

            else -> ScanResult(
                url = cleanUrl,
                status = ScanStatus.SAFE,
                message = "No obvious threat found.",
                reasons = listOf("Local scan passed."),
                score = finalScore
            )
        }
    }

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()

        return when {
            trimmed.isEmpty() -> ""
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "https://$trimmed"
        }
    }

    private fun getHost(url: String): String {
        return try {
            URI(url).host?.lowercase() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
