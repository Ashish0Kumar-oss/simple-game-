package com.example.game

import com.example.audio.HapticManager
import com.example.audio.SoundManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class ChandrayaanEngine(
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    private val onMissionComplete: (score: Int, success: Boolean) -> Unit
) {
    var viewWidth = 1080f
    var viewHeight = 1920f

    // Physical state
    var posX = 540f
    var posY = 200f
    var velX = 0f
    var velY = 40f
    var angleDeg = 0f
    var fuel = 100f

    // Thruster states
    var isMainThrusterActive = false
    var isLeftThrusterActive = false
    var isRightThrusterActive = false

    // Constants
    private val lunarGravity = 140f
    private val mainThrustPower = 340f
    private val rcsPower = 60f
    private val maxSafeLandingVelY = 105f
    private val maxSafeLandingVelX = 55f
    private val maxSafeAngle = 14f

    // Landing zone
    var padX = 540f
    var padY = 1500f
    var padWidth = 240f

    var status: LandingStatus = LandingStatus.Descending
    var roverDeployProgress = 0f

    enum class LandingStatus {
        Descending,
        LandedSuccess,
        Crashed
    }

    fun reset(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
        padY = height * 0.82f
        padX = width * 0.5f
        padWidth = width * 0.36f

        posX = width * (0.35f + (Math.random() * 0.3f).toFloat())
        posY = height * 0.14f
        velX = ((Math.random() - 0.5) * 60f).toFloat()
        velY = 30f
        angleDeg = ((Math.random() - 0.5) * 10f).toFloat()
        fuel = 100f

        isMainThrusterActive = false
        isLeftThrusterActive = false
        isRightThrusterActive = false

        status = LandingStatus.Descending
        roverDeployProgress = 0f
    }

    fun update(dt: Float) {
        if (status == LandingStatus.LandedSuccess) {
            roverDeployProgress = (roverDeployProgress + dt * 0.8f).coerceAtMost(1f)
            return
        }
        if (status == LandingStatus.Crashed) return

        // Thruster forces
        var thrustY = 0f
        var thrustX = 0f

        if (isMainThrusterActive && fuel > 0f) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val force = mainThrustPower
            thrustY -= (force * cos(rad)).toFloat()
            thrustX += (force * sin(rad)).toFloat()
            fuel = (fuel - dt * 14f).coerceAtLeast(0f)
            soundManager.playThruster()
            hapticManager.tick()
        }

        if (isLeftThrusterActive && fuel > 0f) {
            angleDeg = (angleDeg + 35f * dt).coerceIn(-45f, 45f)
            thrustX += rcsPower
            fuel = (fuel - dt * 6f).coerceAtLeast(0f)
        }

        if (isRightThrusterActive && fuel > 0f) {
            angleDeg = (angleDeg - 35f * dt).coerceIn(-45f, 45f)
            thrustX -= rcsPower
            fuel = (fuel - dt * 6f).coerceAtLeast(0f)
        }

        // Apply forces
        velY += (lunarGravity + thrustY) * dt
        velX += thrustX * dt

        posX += velX * dt
        posY += velY * dt

        // Keep inside bounds horizontally
        if (posX < 40f) {
            posX = 40f
            velX = 0f
        } else if (posX > viewWidth - 40f) {
            posX = viewWidth - 40f
            velX = 0f
        }

        // Ground contact check
        val landerRadius = 38f
        if (posY + landerRadius >= padY) {
            posY = padY - landerRadius
            checkLanding()
        }
    }

    private fun checkLanding() {
        val padLeft = padX - padWidth * 0.5f
        val padRight = padX + padWidth * 0.5f

        val withinPad = posX >= padLeft && posX <= padRight
        val safeVelY = velY <= maxSafeLandingVelY
        val safeVelX = abs(velX) <= maxSafeLandingVelX
        val safeAngle = abs(angleDeg) <= maxSafeAngle

        if (withinPad && safeVelY && safeVelX && safeAngle) {
            status = LandingStatus.LandedSuccess
            soundManager.playLandingSuccess()
            hapticManager.powerUp()
            val score = (fuel * 15f + (maxSafeLandingVelY - velY) * 5f).toInt().coerceAtLeast(100)
            onMissionComplete(score, true)
        } else {
            status = LandingStatus.Crashed
            soundManager.playCrash()
            hapticManager.collision()
            onMissionComplete(0, false)
        }
    }
}
