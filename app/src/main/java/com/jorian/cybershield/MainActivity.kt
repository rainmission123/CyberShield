package com.jorian.cybershield

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
    private lateinit var drawerScrim: View
    private lateinit var drawerPanel: LinearLayout

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
        drawerScrim = findViewById(R.id.drawerScrim)
        drawerPanel = findViewById(R.id.drawerPanel)

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
            openSideMenu()
        }

        setupSideMenu()
        setupBackNavigation()

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

    private fun setupSideMenu() {
        drawerPanel.post {
            val width = (resources.displayMetrics.widthPixels * 0.58f).toInt()
            drawerPanel.layoutParams = drawerPanel.layoutParams.apply {
                this.width = width
            }
            drawerPanel.translationX = -width.toFloat()
        }

        drawerScrim.setOnClickListener {
            closeSideMenu()
        }

        findViewById<TextView>(R.id.menuHistory).setOnClickListener {
            closeSideMenu()
            openHistory()
        }

        findViewById<TextView>(R.id.menuRealtime).setOnClickListener {
            closeSideMenu()
            openAccessibilitySettings()
        }

        findViewById<TextView>(R.id.menuDefault).setOnClickListener {
            closeSideMenu()
            openLinkProtectorSettings()
        }

        findViewById<TextView>(R.id.menuAbout).setOnClickListener {
            closeSideMenu()
            showAboutDialog()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawerPanel.visibility == View.VISIBLE) {
                        closeSideMenu()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun openSideMenu() {
        drawerScrim.alpha = 0f
        drawerScrim.visibility = View.VISIBLE
        drawerPanel.visibility = View.VISIBLE

        drawerScrim.animate()
            .alpha(1f)
            .setDuration(180)
            .start()

        drawerPanel.animate()
            .translationX(0f)
            .setDuration(220)
            .start()
    }

    private fun closeSideMenu() {
        drawerScrim.animate()
            .alpha(0f)
            .setDuration(160)
            .withEndAction {
                drawerScrim.visibility = View.GONE
            }
            .start()

        drawerPanel.animate()
            .translationX(-drawerPanel.width.toFloat())
            .setDuration(200)
            .withEndAction {
                drawerPanel.visibility = View.GONE
            }
            .start()
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
