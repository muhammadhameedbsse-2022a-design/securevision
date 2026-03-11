package com.securevision.feature.live

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.securevision.core.domain.model.AlertSeverity

/**
 * Provides haptic and (optional) audible feedback when an alert fires.
 * Sound playback is left as a hook – callers can set [onSoundRequested]
 * to plug in their own [android.media.MediaPlayer] or notification tone.
 */
class AlertFeedbackProvider(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Optional callback invoked when the engine determines that an audible
     * alert should be played.  Hook your preferred sound mechanism here.
     */
    var onSoundRequested: ((AlertSeverity) -> Unit)? = null

    /** Triggers vibration pattern appropriate to the alert severity. */
    fun triggerVibration(severity: AlertSeverity) {
        val vibrator = this.vibrator ?: return
        if (!vibrator.hasVibrator()) return

        val (timings, amplitudes) = when (severity) {
            AlertSeverity.CRITICAL -> longArrayOf(0, 200, 100, 200, 100, 300) to
                intArrayOf(0, 255, 0, 255, 0, 255)
            AlertSeverity.HIGH -> longArrayOf(0, 150, 100, 250) to
                intArrayOf(0, 200, 0, 200)
            AlertSeverity.MEDIUM -> longArrayOf(0, 100, 100, 150) to
                intArrayOf(0, 150, 0, 150)
            AlertSeverity.LOW -> longArrayOf(0, 80) to
                intArrayOf(0, 100)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings.sum())
        }
    }

    /** Triggers the optional sound hook. */
    fun triggerSound(severity: AlertSeverity) {
        onSoundRequested?.invoke(severity)
    }
}
