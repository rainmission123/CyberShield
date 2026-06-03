package com.jorian.cybershield.domain

object SuspiciousRules {

    private val phishingKeywords = listOf(
        "gcash-login",
        "gcash-verify",
        "free-gcash",
        "maya-login",
        "maya-reward",
        "bdo-login",
        "bpi-login",
        "facebook-security",
        "account-locked",
        "verify-account",
        "login-confirm",
        "password-reset",
        "claim-prize",
        "free-load",
        "reward-center",
        "cashback",
        "airdrop",
        "wallet-connect"
    )

    private val shorteners = listOf(
        "bit.ly",
        "tinyurl.com",
        "t.co",
        "shorturl.at",
        "is.gd",
        "cutt.ly",
        "rebrand.ly",
        "s.id"
    )

    private val suspiciousTlds = listOf(
        ".xyz",
        ".top",
        ".click",
        ".work",
        ".monster",
        ".live",
        ".buzz",
        ".ru",
        ".cn"
    )

    private val trustedDomains = listOf(
        "gcash.com",
        "maya.ph",
        "facebook.com",
        "messenger.com",
        "bdo.com.ph",
        "bpi.com.ph",
        "google.com",
        "national-id.gov.ph",
        "gov.ph"
    )

    fun check(url: String): List<String> {
        val reasons = mutableListOf<String>()
        val lowerUrl = url.trim().lowercase()

        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            reasons.add("Invalid or missing http/https URL.")
        }

        val host = runCatching {
            java.net.URI(lowerUrl).host.orEmpty()
        }.getOrDefault("")

        if (trustedDomains.any { host == it || host.endsWith(".$it") }) {
            return reasons
        }

        phishingKeywords.forEach {
            if (lowerUrl.contains(it)) {
                reasons.add("Phishing keyword detected: $it")
            }
        }

        shorteners.forEach {
            if (lowerUrl.contains(it)) {
                reasons.add("URL shortener detected: $it")
            }
        }

        suspiciousTlds.forEach {
            if (lowerUrl.contains(it)) {
                reasons.add("Suspicious domain extension detected: $it")
            }
        }

        if (lowerUrl.contains("@")) {
            reasons.add("URL contains @ symbol, possible phishing trick.")
        }

        if (lowerUrl.length > 120) {
            reasons.add("Very long URL detected.")
        }

        if (lowerUrl.contains("http://")) {
            reasons.add("Link uses insecure HTTP.")
        }

        if (lowerUrl.contains("gcash") && !lowerUrl.contains("gcash.com")) {
            reasons.add("Possible fake GCash website.")
        }

        if (lowerUrl.contains("maya") && !lowerUrl.contains("maya.ph")) {
            reasons.add("Possible fake Maya website.")
        }

        if (lowerUrl.contains("facebook") && !lowerUrl.contains("facebook.com")) {
            reasons.add("Possible fake Facebook website.")
        }

        return reasons.distinct()
    }
}
