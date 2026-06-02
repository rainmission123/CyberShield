package com.jorian.cybershield.domain

object FakeLoginDetector {

    data class FakeLoginResult(
        val score: Int,
        val reasons: List<String>
    )

    fun detect(url: String, host: String): FakeLoginResult {
        val lowerUrl = url.lowercase()
        val lowerHost = host.lowercase()

        val reasons = mutableListOf<String>()
        var score = 0

        val loginPaths = listOf(
            "/login",
            "/signin",
            "/sign-in",
            "/auth",
            "/verify",
            "/verification",
            "/account",
            "/account-security",
            "/secure",
            "/security-check",
            "/password",
            "/password-reset",
            "/reset-password",
            "/update-account"
        )

        val hasLoginPath = loginPaths.any { lowerUrl.contains(it) }

        if (hasLoginPath) {
            score += 20
            reasons.add("Login or account verification page detected.")
        }

        val credentialWords = listOf(
            "password",
            "otp",
            "pin",
            "code",
            "verify",
            "login",
            "signin",
            "account",
            "credential"
        )

        val credentialHits = credentialWords.count { lowerUrl.contains(it) }

        if (credentialHits >= 2) {
            score += 25
            reasons.add("Multiple credential-related keywords detected.")
        }

        val brandKeywords = listOf(
            "gcash",
            "maya",
            "paypal",
            "facebook",
            "messenger",
            "bpi",
            "bdo",
            "metrobank",
            "landbank",
            "unionbank"
        )

        val detectedBrand = brandKeywords.firstOrNull {
            lowerUrl.contains(it)
        }

        if (detectedBrand != null && !isOfficialBrandDomain(lowerHost, detectedBrand)) {
            score += 40
            reasons.add("Fake login page targeting ${detectedBrand.uppercase()} detected.")
        }

        if (
            detectedBrand != null &&
            hasLoginPath &&
            !isOfficialBrandDomain(lowerHost, detectedBrand)
        ) {
            score += 35
            reasons.add("Brand name combined with login/verify page on unofficial domain.")
        }

        val phishingCombos = listOf(
            "login" to "verify",
            "account" to "verify",
            "password" to "reset",
            "security" to "check",
            "claim" to "reward",
            "free" to "reward",
            "bonus" to "claim",
            "wallet" to "verify"
        )

        phishingCombos.forEach { combo ->
            if (lowerUrl.contains(combo.first) && lowerUrl.contains(combo.second)) {
                score += 15
                reasons.add("Phishing keyword combination detected: ${combo.first} + ${combo.second}")
            }
        }

        if (lowerUrl.contains("login") && lowerUrl.contains("free")) {
            score += 25
            reasons.add("Fake login lure detected using free/reward wording.")
        }

        return FakeLoginResult(
            score = score.coerceIn(0, 100),
            reasons = reasons.distinct()
        )
    }

    private fun isOfficialBrandDomain(host: String, brand: String): Boolean {
        return when (brand) {
            "gcash" -> host == "gcash.com" || host.endsWith(".gcash.com")
            "maya" -> host == "maya.ph" || host.endsWith(".maya.ph")
            "paypal" -> host == "paypal.com" || host.endsWith(".paypal.com")
            "facebook" -> host == "facebook.com" || host.endsWith(".facebook.com")
            "messenger" -> host == "messenger.com" || host.endsWith(".messenger.com")
            "bpi" -> host == "bpi.com.ph" || host.endsWith(".bpi.com.ph")
            "bdo" -> host == "bdo.com.ph" || host.endsWith(".bdo.com.ph")
            "metrobank" -> host == "metrobank.com.ph" || host.endsWith(".metrobank.com.ph")
            "landbank" -> host == "landbank.com" || host.endsWith(".landbank.com")
            "unionbank" -> host == "unionbankph.com" || host.endsWith(".unionbankph.com")
            else -> false
        }
    }
}