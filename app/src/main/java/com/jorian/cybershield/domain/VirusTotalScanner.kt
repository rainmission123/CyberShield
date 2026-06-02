package com.jorian.cybershield.domain

import com.jorian.cybershield.BuildConfig
import com.jorian.cybershield.network.RetrofitClient
import kotlinx.coroutines.delay

class VirusTotalScanner {

    data class VirusTotalResult(
        val success: Boolean,
        val malicious: Int = 0,
        val suspicious: Int = 0,
        val harmless: Int = 0,
        val undetected: Int = 0,
        val message: String = ""
    )

    private val apiKey = BuildConfig.VIRUSTOTAL_API_KEY

    suspend fun scanUrl(
        url: String
    ): VirusTotalResult {

        if (apiKey.isBlank() || apiKey == "null") {
            return VirusTotalResult(
                success = false,
                message = "VirusTotal API key missing."
            )
        }

        return try {

            val submitResponse = RetrofitClient.virusTotalApi.submitUrl(
                apiKey = apiKey,
                url = url
            )

            if (!submitResponse.isSuccessful) {
                return VirusTotalResult(
                    success = false,
                    message = "VirusTotal submit failed: ${submitResponse.code()}"
                )
            }

            val analysisId = submitResponse.body()
                ?.data
                ?.id

            if (analysisId.isNullOrEmpty()) {
                return VirusTotalResult(
                    success = false,
                    message = "VirusTotal did not return analysis ID."
                )
            }

            delay(3000)

            val analysisResponse = RetrofitClient.virusTotalApi.getAnalysis(
                apiKey = apiKey,
                analysisId = analysisId
            )

            if (!analysisResponse.isSuccessful) {
                return VirusTotalResult(
                    success = false,
                    message = "VirusTotal analysis failed: ${analysisResponse.code()}"
                )
            }

            val stats = analysisResponse.body()
                ?.data
                ?.attributes
                ?.stats

            if (stats == null) {
                return VirusTotalResult(
                    success = false,
                    message = "VirusTotal returned empty stats."
                )
            }

            VirusTotalResult(
                success = true,
                malicious = stats.malicious,
                suspicious = stats.suspicious,
                harmless = stats.harmless,
                undetected = stats.undetected,
                message = "VirusTotal scan complete."
            )

        } catch (e: Exception) {

            VirusTotalResult(
                success = false,
                message = e.message ?: "VirusTotal scan error."
            )
        }
    }
}