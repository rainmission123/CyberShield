package com.jorian.cybershield

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jorian.cybershield.domain.IncomingLinkHandler
import com.jorian.cybershield.domain.UrlScannerManager
import com.jorian.cybershield.ui.HistoryActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnEnableProtection: Button
    private lateinit var btnHistory: Button
    private lateinit var btnSetDefault: Button
    private lateinit var tvMenu: TextView
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
        tvMenu = findViewById(R.id.tvMenu)
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
            openLinkProtectorSettings()
        }

        tvMenu.setOnClickListener {
            showCyberShieldMenu()
        }

        incomingLinkHandler.handle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingLinkHandler.handle(intent)
    }

    override fun onDestroy() {
        incomingLinkHandler.cancelOngoingScan()
        super.onDestroy()
    }

    private fun showCyberShieldMenu() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_cyber_menu)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<TextView>(R.id.menuHistory).setOnClickListener {
            dialog.dismiss()
            openHistory()
        }

        dialog.findViewById<TextView>(R.id.menuRealtime).setOnClickListener {
            dialog.dismiss()
            openAccessibilitySettings()
        }

        dialog.findViewById<TextView>(R.id.menuDefault).setOnClickListener {
            dialog.dismiss()
            openLinkProtectorSettings()
        }

        dialog.findViewById<TextView>(R.id.menuAbout).setOnClickListener {
            dialog.dismiss()
            showAboutDialog()
        }

        dialog.show()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun openHistory() {
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    private fun openAccessibilitySettings() {
        openSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun openDefaultBrowserSettings() {
        openSettings(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }

    private fun openLinkProtectorSettings() {
        Toast.makeText(
            this,
            "Tap Browser app, then choose CyberShield.",
            Toast.LENGTH_LONG
        ).show()
        openDefaultBrowserSettings()
    }

    private fun openSettings(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (fallbackError: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Settings app is not available.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearCurrentScan() {
        etUrl.text?.clear()
        tvResult.text = "Result will appear here."
        tvReasons.text = ""
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("CyberShield")
            .setMessage(
                "CyberShield scans suspicious links using local phishing rules and optional VirusTotal cloud scanning."
            )
            .setPositiveButton("OK", null)
            .show()
    }
}
