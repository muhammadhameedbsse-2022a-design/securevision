package com.securevision.core.domain.usecase

import com.securevision.core.domain.model.DetectionType
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks per-[DetectionType] cooldowns so that repeated detections
 * within a short window do not produce spam alerts.
 *
 * Thread-safe – may be called from any coroutine dispatcher.
 */
class AlertCooldownManager(
    private val defaultCooldownMs: Long = DEFAULT_COOLDOWN_MS
) {

    private val lastAlertTimes = ConcurrentHashMap<DetectionType, Long>()

    /**
     * Returns `true` if enough time has elapsed since the last alert of the
     * given [type], and atomically records the current time. Returns `false`
     * (and does nothing) when the cooldown has not yet expired.
     *
     * Thread-safe: uses [ConcurrentHashMap.compute] for atomic
     * check-and-update so concurrent callers cannot both pass the gate.
     */
    fun shouldTriggerAlert(
        type: DetectionType,
        cooldownMs: Long = defaultCooldownMs
    ): Boolean {
        val now = System.currentTimeMillis()
        var triggered = false
        lastAlertTimes.compute(type) { _, lastTime ->
            if (now - (lastTime ?: 0L) >= cooldownMs) {
                triggered = true
                now
            } else {
                lastTime
            }
        }
        return triggered
    }

    /** Resets cooldown state for all detection types. */
    fun reset() {
        lastAlertTimes.clear()
    }

    companion object {
        /** Default cooldown: 30 seconds between alerts of the same type. */
        const val DEFAULT_COOLDOWN_MS = 30_000L

        /** Shorter cooldown for critical alerts (weapons). */
        const val WEAPON_COOLDOWN_MS = 15_000L

        /** Longer cooldown for lower-priority alerts (unknown persons). */
        const val UNKNOWN_PERSON_COOLDOWN_MS = 60_000L
    }
}
