package com.jorian.cybershield.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jorian.cybershield.domain.UrlScannerManager
import com.jorian.cybershield.model.ScanStatus
import com.jorian.cybershield.ui.WarningActivity
import com.jorian.cybershield.utils.AlertSoundHelper
import com.jorian.cybershield.utils.VibrationHelper
import java.util.ArrayList

@SuppressLint("AccessibilityPolicy")
class CyberShieldAccessibilityService : AccessibilityService() {

    private val scanner = UrlScannerManager()

    private var lastDetectedUrl: String = ""
    private var lastAlertTime: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val screenText = buildString {
            event.text?.forEach {
                append(it.toString()).append(" ")
            }

            event.contentDescription?.let {
                append(it.toString()).append(" ")
            }

            append(collectTextFromNode(rootInActiveWindow))
        }

        if (screenText.isBlank()) return

        val url = extractUrl(screenText) ?: return

        if (shouldIgnoreDuplicate(url)) return

        val result = scanner.scan(url)

        if (
            result.status == ScanStatus.SUSPICIOUS ||
            result.status == ScanStatus.DANGEROUS
        ) {
            lastDetectedUrl = result.url
            lastAlertTime = System.currentTimeMillis()

            VibrationHelper.dangerAlert(this)
            AlertSoundHelper.playWarningSound(this)

            openWarningPage(
                url = result.url,
                score = result.score,
                status = result.status.name,
                reasons = result.reasons
            )
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    private fun collectTextFromNode(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        val builder = StringBuilder()

        fun traverse(currentNode: AccessibilityNodeInfo?) {
            if (currentNode == null) return

            val text = currentNode.text?.toString()
            val content = currentNode.contentDescription?.toString()

            if (!text.isNullOrBlank()) {
                builder.append(text).append(" ")
            }

            if (!content.isNullOrBlank()) {
                builder.append(content).append(" ")
            }

            for (i in 0 until currentNode.childCount) {
                traverse(currentNode.getChild(i))
            }
        }

        traverse(node)

        return builder.toString()
    }

    private fun extractUrl(text: String): String? {
        val regex = Regex(
            pattern = """(https?://\S+|www\.\S+)""",
            option = RegexOption.IGNORE_CASE
        )

        val match = regex.find(text)?.value ?: return null

        val cleaned = match
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "")
            .trim()
            .trimEnd('.', ',', ')', ']', '}', '"', '\'')

        if (!isValidRealUrl(cleaned)) {
            return null
        }

        return when {
            cleaned.startsWith("http://", true) ||
                    cleaned.startsWith("https://", true) -> cleaned

            cleaned.startsWith("www.", true) -> "https://$cleaned"

            else -> null
        }
    }

    private fun isValidRealUrl(url: String): Boolean {
        val lower = url.lowercase()

        if (
            lower.contains("com.facebook") ||
            lower.contains("android.") ||
            lower.contains("widget") ||
            lower.contains("layout") ||
            lower.contains("viewgroup") ||
            lower.contains("resourceid")
        ) {
            return false
        }

        if (
            !lower.startsWith("http://") &&
            !lower.startsWith("https://") &&
            !lower.startsWith("www.")
        ) {
            return false
        }

        val domainRegex = Regex(
            pattern = """^(https?://|www\.)[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+(/[^\s]*)?$""",
            option = RegexOption.IGNORE_CASE
        )

        return domainRegex.matches(url)
    }

    private fun shouldIgnoreDuplicate(url: String): Boolean {
        val now = System.currentTimeMillis()
        val normalizedUrl = url.trim().lowercase()
        val normalizedLastUrl = lastDetectedUrl.trim().lowercase()

        val isSameUrl = normalizedUrl == normalizedLastUrl
        val isTooSoon = now - lastAlertTime < 10_000

        return isSameUrl && isTooSoon
    }

    private fun openWarningPage(
        url: String,
        score: Int,
        status: String,
        reasons: List<String>
    ) {
        val intent = Intent(this, WarningActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(WarningActivity.EXTRA_URL, url)
            putExtra(WarningActivity.EXTRA_SCORE, score)
            putExtra(WarningActivity.EXTRA_STATUS, status)
            putStringArrayListExtra(
                WarningActivity.EXTRA_REASONS,
                ArrayList(reasons)
            )
        }

        startActivity(intent)
    }
}