package com.jorian.cybershield.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.jorian.cybershield.R
import com.jorian.cybershield.utils.AlertSoundHelper

class WarningActivity : Activity() {

    private lateinit var warningRoot: ScrollView
    private lateinit var tvWarningTitle: TextView
    private lateinit var tvBlockedUrl: TextView
    private lateinit var tvPhishingScore: TextView
    private lateinit var tvWarningReasons: TextView
    private lateinit var btnGoBack: Button
    private lateinit var btnOpenAnyway: Button

    private val flashHandler = Handler(Looper.getMainLooper())
    private var isRed = false
    private var blockedUrl: String = ""

    private val flashRunnable = object : Runnable {
        override fun run() {
            warningRoot.setBackgroundColor(
                if (isRed) Color.parseColor("#220000")
                else Color.parseColor("#001833")
            )

            isRed = !isRed
            flashHandler.postDelayed(this, 450)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning)

        warningRoot = findViewById(R.id.warningRoot)
        tvWarningTitle = findViewById(R.id.tvWarningTitle)
        tvBlockedUrl = findViewById(R.id.tvBlockedUrl)
        tvPhishingScore = findViewById(R.id.tvPhishingScore)
        tvWarningReasons = findViewById(R.id.tvWarningReasons)
        btnGoBack = findViewById(R.id.btnGoBack)
        btnOpenAnyway = findViewById(R.id.btnOpenAnyway)

        blockedUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val status = intent.getStringExtra(EXTRA_STATUS) ?: "WARNING"
        val reasons = intent.getStringArrayListExtra(EXTRA_REASONS) ?: arrayListOf()

        tvWarningTitle.text = when (status) {
            "DANGEROUS" -> "🚨 Dangerous Link Blocked"
            "SUSPICIOUS" -> "⚠️ Suspicious Link Warning"
            else -> "⚠️ Link Warning"
        }

        tvBlockedUrl.text = blockedUrl
        tvPhishingScore.text = "Phishing Score: $score%"

        tvWarningReasons.text = if (reasons.isNotEmpty()) {
            reasons.joinToString(separator = "\n") { "• $it" }
        } else {
            "• This link may be unsafe."
        }

        startFlashingEffect()

        btnGoBack.setOnClickListener {
            stopAlertEffects()
            finish()
        }

        btnOpenAnyway.setOnClickListener {
            openBlockedUrl()
        }
    }

    private fun startFlashingEffect() {
        flashHandler.post(flashRunnable)
    }

    private fun stopFlashingEffect() {
        flashHandler.removeCallbacks(flashRunnable)
        warningRoot.setBackgroundColor(Color.parseColor("#FFF5F5"))
    }

    private fun stopAlertEffects() {
        stopFlashingEffect()
        AlertSoundHelper.stopWarningSound()
    }

    private fun openBlockedUrl() {
        stopAlertEffects()

        if (blockedUrl.isNotEmpty()) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(blockedUrl))
                startActivity(browserIntent)
                finish()
            } catch (e: ActivityNotFoundException) {
                tvWarningReasons.append("\nNo browser app is available to open this link.")
            }
        }
    }

    override fun onDestroy() {
        stopAlertEffects()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_SCORE = "extra_score"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_REASONS = "extra_reasons"
    }
}
