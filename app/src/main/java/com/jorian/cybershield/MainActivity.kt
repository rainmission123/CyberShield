package com.jorian.cybershield

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jorian.cybershield.domain.IncomingLinkHandler
import com.jorian.cybershield.domain.UrlScannerManager
import com.jorian.cybershield.ui.HistoryActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnEnableProtection: Button
    private lateinit var btnHistory: Button
    private lateinit var btnSetDefault: Button
    private lateinit var etUrl: EditText
    private lateinit var btnScan: Button
    private lateinit var tvResult: TextView
    private lateinit var tvReasons: TextView

    private val scanner = UrlScannerManager()
    private lateinit var incomingLinkHandler: IncomingLinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnEnableProtection = findViewById(R.id.btnEnableProtection)
        btnHistory = findViewById(R.id.btnHistory)
        btnSetDefault = findViewById(R.id.btnSetDefault)
        etUrl = findViewById(R.id.etUrl)
        btnScan = findViewById(R.id.btnScan)
        tvResult = findViewById(R.id.tvResult)
        tvReasons = findViewById(R.id.tvReasons)

        incomingLinkHandler = IncomingLinkHandler(
            activity = this,
            scanner = scanner,
            etUrl = etUrl,
            tvResult = tvResult,
            tvReasons = tvReasons
        )

        btnEnableProtection.setOnClickListener {
            openAccessibilitySettings()
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnScan.setOnClickListener {
            incomingLinkHandler.scanManualInput()
        }

        btnSetDefault.setOnClickListener {
            openDefaultBrowserSettings()
        }

        incomingLinkHandler.handle(intent)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun openDefaultBrowserSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        startActivity(intent)
    }
}