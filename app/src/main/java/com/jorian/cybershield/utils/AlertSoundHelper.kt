package com.jorian.cybershield.utils

import android.content.Context
import android.media.MediaPlayer
import com.jorian.cybershield.R

object AlertSoundHelper {

    private var mediaPlayer: MediaPlayer? = null

    fun playWarningSound(context: Context) {

        stopWarningSound()

        mediaPlayer = MediaPlayer.create(
            context,
            R.raw.warning_alert
        )

        mediaPlayer?.isLooping = false

        mediaPlayer?.setOnCompletionListener {
            stopWarningSound()
        }

        mediaPlayer?.start()
    }

    fun stopWarningSound() {

        runCatching {
            mediaPlayer?.stop()
        }

        runCatching {
            mediaPlayer?.release()
        }

        mediaPlayer = null
    }
}
