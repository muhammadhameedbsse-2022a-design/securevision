package com.securevision.feature.live

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.securevision.core.domain.model.AlertSeverity
import java.util.concurrent.atomic.AtomicInteger

/**
 * Provides haptic, audible, and push notification feedback when an alert fires.
 */
class AlertFeedbackProvider(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private val notificationIdCounter = AtomicInteger(1000)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SecureVision Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time detection alerts from SecureVision"
                enableVibration(false) // We handle vibration ourselves
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

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

    /** Plays the default notification/alarm sound based on severity. */
    fun triggerSound(severity: AlertSeverity) {
        val ringtoneType = when (severity) {
            AlertSeverity.CRITICAL, AlertSeverity.HIGH -> RingtoneManager.TYPE_ALARM
            else -> RingtoneManager.TYPE_NOTIFICATION
        }
        try {
            val uri = RingtoneManager.getDefaultUri(ringtoneType)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        } catch (_: Exception) {
            // Silently ignore if sound cannot be played
        }
    }

    /** Posts a system push notification for the alert. */
    fun triggerNotification(title: String, description: String, severity: AlertSeverity) {
        val priority = when (severity) {
            AlertSeverity.CRITICAL -> NotificationCompat.PRIORITY_MAX
            AlertSeverity.HIGH -> NotificationCompat.PRIORITY_HIGH
            AlertSeverity.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            AlertSeverity.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(notificationIdCounter.getAndIncrement(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "securevision_alerts"
    }
}
