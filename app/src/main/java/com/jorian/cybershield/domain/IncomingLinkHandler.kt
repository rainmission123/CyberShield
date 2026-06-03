package com.jorian.cybershield.domain

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import android.widget.TextView
import com.jorian.cybershield.data.ScanHistoryRepository
import com.jorian.cybershield.data.VirusTotalCacheRepository
import com.jorian.cybershield.model.ScanStatus
import com.jorian.cybershield.ui.WarningActivity
import com.jorian.cybershield.utils.AlertSoundHelper
import com.jorian.cybershield.utils.VibrationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class IncomingLinkHandler(
    private val activity: Activity,
    private val scanner: UrlScannerManager,
    private val etUrl: EditText,
    private val tvResult: TextView,
    private val tvReasons: TextView
) {

    private val historyRepository = ScanHistoryRepository(activity)
    private val virusTotalScanner = VirusTotalScanner()
    private val virusTotalCache = VirusTotalCacheRepository(activity)

    private var scanJob: Job? = null
    private var lastScanUrl: String = ""
    private var lastScanTime: Long = 0L
    private var isScanning = false

    fun handle(intent: Intent?) {
        val incomingUrl = intent?.data?.toString()?.trim()

        if (!incomingUrl.isNullOrEmpty()) {
            etUrl.setText(incomingUrl)
            scanAndHandle(incomingUrl)
        }
    }

    fun scanManualInput() {
        val url = etUrl.text.toString().trim()

        if (url.isEmpty()) {
            tvResult.text = "⚠️ EMPTY URL"
            tvReasons.text = "• Please paste a link first."
            return
        }

        scanAndHandle(url)
    }

    private fun scanAndHandle(url: String) {
        val normalizedInput = url.trim().lowercase()
        val now = System.currentTimeMillis()

        if (isScanning) {
            tvReasons.text = "• Scan already running. Please wait..."
            return
        }

        if (normalizedInput == lastScanUrl && now - lastScanTime < 8_000) {
            tvReasons.text = "• Same link was scanned recently."
            return
        }

        lastScanUrl = normalizedInput
        lastScanTime = now
        isScanning = true

        scanJob?.cancel()

        val localResult = scanner.scan(url)

        tvResult.text = when (localResult.status) {
            ScanStatus.SAFE -> "✅ SAFE\n${localResult.message}"
            ScanStatus.SUSPICIOUS -> "⚠️ SUSPICIOUS\n${localResult.message}\nChecking cloud cache..."
            ScanStatus.DANGEROUS -> "🚨 DANGEROUS\n${localResult.message}\nChecking cloud cache..."
        }

        tvReasons.text = localResult.reasons.joinToString(separator = "\n") { "• $it" }

        if (localResult.status == ScanStatus.SAFE) {
            historyRepository.saveScan(
                url = localResult.url,
                status = localResult.status,
                score = localResult.score
            )

            isScanning = false
            openInBrowser(localResult.url)
            return
        }

        scanJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val cachedResult = withContext(Dispatchers.IO) {
                    virusTotalCache.getCachedResult(localResult.url)
                }

                val vtResult = if (cachedResult != null) {
                    cachedResult.copy(
                        message = "VirusTotal result loaded from cache."
                    )
                } else {
                    val freshResult = withContext(Dispatchers.IO) {
                        virusTotalScanner.scanUrl(localResult.url)
                    }

                    if (freshResult.success) {
                        withContext(Dispatchers.IO) {
                            virusTotalCache.saveResult(localResult.url, freshResult)
                        }
                    }

                    freshResult
                }

                val finalReasons = localResult.reasons.toMutableList()
                var finalScore = localResult.score

                if (vtResult.success) {
                    finalReasons.add(vtResult.message)
                    finalReasons.add("VirusTotal malicious detections: ${vtResult.malicious}")
                    finalReasons.add("VirusTotal suspicious detections: ${vtResult.suspicious}")

                    if (vtResult.malicious > 0) {
                        finalScore += 25
                        finalReasons.add("VirusTotal vendors reported this URL as malicious.")
                    }

                    if (vtResult.suspicious > 0) {
                        finalScore += 15
                        finalReasons.add("VirusTotal vendors reported this URL as suspicious.")
                    }
                } else {
                    finalReasons.add("VirusTotal unavailable: ${vtResult.message}")
                }

                finalScore = finalScore.coerceIn(0, 100)

                val finalStatus = when {
                    finalScore >= 60 -> ScanStatus.DANGEROUS
                    finalScore >= 25 -> ScanStatus.SUSPICIOUS
                    else -> ScanStatus.SAFE
                }

                historyRepository.saveScan(
                    url = localResult.url,
                    status = finalStatus,
                    score = finalScore
                )

                tvResult.text = when (finalStatus) {
                    ScanStatus.SAFE -> "✅ SAFE\nCloud scan passed."
                    ScanStatus.SUSPICIOUS -> "⚠️ SUSPICIOUS\nLocal + cloud scan completed."
                    ScanStatus.DANGEROUS -> "🚨 DANGEROUS\nLocal + cloud threat detected."
                }

                tvReasons.text = finalReasons.distinct()
                    .joinToString(separator = "\n") { "• $it" }

                when (finalStatus) {
                    ScanStatus.SAFE -> {
                        openInBrowser(localResult.url)
                    }

                    ScanStatus.SUSPICIOUS,
                    ScanStatus.DANGEROUS -> {
                        VibrationHelper.dangerAlert(activity)
                        AlertSoundHelper.playWarningSound(activity)

                        openWarningPage(
                            url = localResult.url,
                            score = finalScore,
                            status = finalStatus.name,
                            reasons = finalReasons.distinct()
                        )
                    }
                }
            } finally {
                isScanning = false
            }
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(browserIntent)
            activity.finish()
        } catch (e: ActivityNotFoundException) {
            tvResult.text = "SAFE\n${scanner.scan(url).message}"
            tvReasons.text = "No browser app is available to open this link."
        }
    }

    private fun openWarningPage(
        url: String,
        score: Int,
        status: String,
        reasons: List<String>
    ) {
        val intent = Intent(activity, WarningActivity::class.java).apply {
            putExtra(WarningActivity.EXTRA_URL, url)
            putExtra(WarningActivity.EXTRA_SCORE, score)
            putExtra(WarningActivity.EXTRA_STATUS, status)
            putStringArrayListExtra(
                WarningActivity.EXTRA_REASONS,
                ArrayList(reasons)
            )
        }

        activity.startActivity(intent)
    }
}
