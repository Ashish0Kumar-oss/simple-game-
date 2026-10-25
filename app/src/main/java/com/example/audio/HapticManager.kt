package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(
    private val context: Context,
    private val isHapticsEnabled: () -> Boolean
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun tick() {
        if (!isHapticsEnabled()) return
        vibrate(durationMs = 15, amplitude = 40)
    }

    fun jump() {
        if (!isHapticsEnabled()) return
        vibrate(durationMs = 25, amplitude = 80)
    }

    fun collect() {
        if (!isHapticsEnabled()) return
        vibrate(durationMs = 30, amplitude = 120)
    }

    fun powerUp() {
        if (!isHapticsEnabled()) return
        vibrate(durationMs = 70, amplitude = 180)
    }

    fun collision() {
        if (!isHapticsEnabled()) return
        vibrate(durationMs = 180, amplitude = 255)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
