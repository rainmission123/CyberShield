package com.jorian.cybershield.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object ExternalUrlOpener {

    fun open(context: Context, url: String): Boolean {
        val normalizedUrl = UrlUtils.normalize(url)
        if (!UrlUtils.isValidUrl(normalizedUrl)) return false

        val baseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        val packageManager = context.packageManager
        val externalHandler = packageManager
            .queryIntentActivities(baseIntent, 0)
            .firstOrNull { it.activityInfo.packageName != context.packageName }

        val intent = if (externalHandler != null) {
            Intent(baseIntent).setPackage(externalHandler.activityInfo.packageName)
        } else {
            Intent.createChooser(baseIntent, "Open link with")
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
