package com.jorian.cybershield.ui

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.jorian.cybershield.data.ScanHistoryRepository
import com.jorian.cybershield.model.HistoryItem
import com.jorian.cybershield.model.ScanStatus

class HistoryActivity : Activity() {

    private lateinit var repository: ScanHistoryRepository
    private lateinit var container: LinearLayout
    private lateinit var btnClearHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = ScanHistoryRepository(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#010703"))
            setPadding(28, 48, 28, 28)
        }

        val title = TextView(this).apply {
            text = "CyberShield Logs"
            textSize = 30f
            setTextColor(Color.parseColor("#00FF41"))
            setShadowLayer(14f, 0f, 0f, Color.parseColor("#00FF41"))
            gravity = Gravity.CENTER
        }

        btnClearHistory = Button(this).apply {
            text = "Clear History"
            isAllCaps = false
            setTextColor(Color.parseColor("#FF3B30"))
            setBackgroundColor(Color.TRANSPARENT)
        }

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scrollView = ScrollView(this).apply {
            addView(container)
        }

        root.addView(title)
        root.addView(btnClearHistory)
        root.addView(scrollView)

        setContentView(root)

        btnClearHistory.setOnClickListener {
            repository.clearHistory()
            loadHistory()
        }

        loadHistory()
    }

    private fun loadHistory() {
        container.removeAllViews()

        val history = repository.getHistory()

        if (history.isEmpty()) {
            container.addView(
                TextView(this).apply {
                    text = "No scan history yet."
                    textSize = 18f
                    setTextColor(Color.parseColor("#A9FFB0"))
                    gravity = Gravity.CENTER
                    setPadding(0, 60, 0, 0)
                }
            )
            return
        }

        history.forEachIndexed { index, item ->
            container.addView(createHistoryCard(item, index))
        }
    }

    private fun createHistoryCard(
        item: HistoryItem,
        position: Int
    ): LinearLayout {
        val statusText = when (item.status) {
            ScanStatus.SAFE -> "✅ SAFE"
            ScanStatus.SUSPICIOUS -> "⚠️ SUSPICIOUS"
            ScanStatus.DANGEROUS -> "🚨 DANGEROUS"
        }

        val statusColor = when (item.status) {
            ScanStatus.SAFE -> Color.parseColor("#00FF41")
            ScanStatus.SUSPICIOUS -> Color.parseColor("#FFD54F")
            ScanStatus.DANGEROUS -> Color.parseColor("#FF3B30")
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 20)
            background = CyberCardDrawable(statusColor)
            isClickable = true
            isLongClickable = true

            setOnLongClickListener {
                AlertDialog.Builder(this@HistoryActivity)
                    .setTitle("Delete history?")
                    .setMessage("Remove this scan record?")
                    .setPositiveButton("Delete") { _, _ ->
                        repository.deleteItemAt(position)
                        loadHistory()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

                true
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 18, 0, 0)
            }

            addView(TextView(context).apply {
                text = statusText
                textSize = 22f
                setTextColor(statusColor)
                setTypeface(null, android.graphics.Typeface.BOLD)
            })

            addView(TextView(context).apply {
                text = "Threat Score: ${item.score}%"
                textSize = 14f
                setTextColor(Color.parseColor("#A9FFB0"))
                setPadding(0, 8, 0, 0)
            })

            addView(TextView(context).apply {
                text = item.url
                textSize = 15f
                setTextColor(Color.WHITE)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setPadding(0, 12, 0, 0)
            })

            addView(TextView(context).apply {
                text = item.dateTime
                textSize = 12f
                setTextColor(Color.parseColor("#63FF78"))
                setPadding(0, 14, 0, 0)
            })
        }
    }

    private class CyberCardDrawable(
        strokeColor: Int
    ) : android.graphics.drawable.GradientDrawable() {
        init {
            shape = RECTANGLE
            cornerRadius = 24f
            setColor(Color.parseColor("#88031008"))
            setStroke(2, strokeColor)
        }
    }
}