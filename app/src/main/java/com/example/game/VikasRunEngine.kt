package com.example.game

import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.model.CollectibleType
import com.example.model.ObstacleType
import com.example.model.PowerUpType
import com.example.model.RunStats
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class RunObstacle(
    val id: Long,
    var x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: ObstacleType,
    val isOverhead: Boolean = false
)

data class RunCollectible(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val type: CollectibleType,
    var isCollected: Boolean = false
)

data class FloatingSpeech(
    val text: String,
    var x: Float,
    var y: Float,
    var alpha: Float = 1.0f,
    var lifeSeconds: Float = 2.2f
)

class VikasRunEngine(
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    private val onGameOver: (RunStats) -> Unit
) {
    var isRunning = false
        private set
    var isPaused = false
    var isGameOver = false
        private set

    // Viewport dimensions
    var viewWidth = 1080f
    var viewHeight = 1920f
    var groundY = 1400f

    // Modi player physical state
    var playerX = 140f
    var playerY = groundY
    var playerVy = 0f
    var isGrounded = true
    var jumpCount = 0
    var isSliding = false
    var slideTimer = 0f
    val playerWidth = 72f
    val playerHeight = 110f

    // Physics
    private val gravity = 1850f
    private val jumpVelocity = -820f
    private val doubleJumpVelocity = -720f

    // Speed & Scrolling
    var baseSpeed = 380f
    var currentSpeed = 380f
    var distanceMeters = 0
    var score = 0
    var coinsCollected = 0
    var broomsCollected = 0
    var cleanCombo = 0
    var maxCleanCombo = 0
    var powerUpsUsed = 0

    // Active Power-ups
    var activeTurboTime = 0f
    var activeShieldTime = 0f
    var activeMagnetTime = 0f
    var activeMultiplierTime = 0f

    val isTurboActive: Boolean get() = activeTurboTime > 0f
    val isShieldActive: Boolean get() = activeShieldTime > 0f
    val isMagnetActive: Boolean get() = activeMagnetTime > 0f
    val isMultiplierActive: Boolean get() = activeMultiplierTime > 0f

    // Game Entities
    val obstacles = mutableListOf<RunObstacle>()
    val collectibles = mutableListOf<RunCollectible>()
    val speechBanners = mutableListOf<FloatingSpeech>()

    // Spawning timers
    private var nextObstacleTimer = 1.8f
    private var nextCollectibleTimer = 0.8f
    private var nextSpeechDistance = 150
    private var entityIdGen = 0L

    var animTime = 0f
        private set

    fun reset(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
        groundY = height * 0.76f

        playerX = width * 0.16f
        playerY = groundY
        playerVy = 0f
        isGrounded = true
        jumpCount = 0
        isSliding = false
        slideTimer = 0f

        currentSpeed = baseSpeed
        distanceMeters = 0
        score = 0
        coinsCollected = 0
        broomsCollected = 0
        cleanCombo = 0
        maxCleanCombo = 0
        powerUpsUsed = 0

        activeTurboTime = 0f
        activeShieldTime = 0f
        activeMagnetTime = 0f
        activeMultiplierTime = 0f

        obstacles.clear()
        collectibles.clear()
        speechBanners.clear()

        nextObstacleTimer = 1.6f
        nextCollectibleTimer = 0.5f
        nextSpeechDistance = 100

        animTime = 0f
        isGameOver = false
        isPaused = false
        isRunning = true

        addSpeechBanner("Mitron! Yatra Starts! 🇮🇳")
    }

    fun jump() {
        if (!isRunning || isPaused || isGameOver) return

        if (isSliding) {
            // Cancel slide immediately on jump
            isSliding = false
            slideTimer = 0f
        }

        if (isGrounded) {
            playerVy = jumpVelocity
            isGrounded = false
            jumpCount = 1
            soundManager.playJump()
            hapticManager.jump()
        } else if (jumpCount == 1) {
            playerVy = doubleJumpVelocity
            jumpCount = 2
            soundManager.playDoubleJump()
            hapticManager.jump()
        }
    }

    fun slide() {
        if (!isRunning || isPaused || isGameOver) return
        if (isGrounded && !isSliding) {
            isSliding = true
            slideTimer = 0.85f
            hapticManager.tick()
        }
    }

    fun update(dt: Float) {
        if (!isRunning || isPaused || isGameOver) return

        animTime += dt

        // Update power-up timers
        if (activeTurboTime > 0f) activeTurboTime = (activeTurboTime - dt).coerceAtLeast(0f)
        if (activeShieldTime > 0f) activeShieldTime = (activeShieldTime - dt).coerceAtLeast(0f)
        if (activeMagnetTime > 0f) activeMagnetTime = (activeMagnetTime - dt).coerceAtLeast(0f)
        if (activeMultiplierTime > 0f) activeMultiplierTime = (activeMultiplierTime - dt).coerceAtLeast(0f)

        // Speed calculation: Turbo multiplies speed by 1.6x
        val difficultySpeed = baseSpeed + (distanceMeters * 0.35f).coerceAtMost(280f)
        currentSpeed = if (isTurboActive) difficultySpeed * 1.55f else difficultySpeed

        // Distance & Base Score
        val pixelsTravelled = currentSpeed * dt
        val oldDist = distanceMeters
        distanceMeters = (distanceMeters + (pixelsTravelled / 24f).toInt())
        val distGained = distanceMeters - oldDist
        if (distGained > 0) {
            score += distGained * (if (isMultiplierActive) 2 else 1)
        }

        // Periodic milestone speech
        if (distanceMeters >= nextSpeechDistance) {
            triggerMilestoneSpeech(distanceMeters)
            nextSpeechDistance += 200
        }

        // Sliding update
        if (isSliding) {
            slideTimer -= dt
            if (slideTimer <= 0f) {
                isSliding = false
            }
        }

        // Jump & Gravity physics
        if (!isGrounded) {
            playerVy += gravity * dt
            playerY += playerVy * dt
            if (playerY >= groundY) {
                playerY = groundY
                playerVy = 0f
                isGrounded = true
                jumpCount = 0
            }
        }

        // Magnet logic: pull nearby collectibles toward Modi
        if (isMagnetActive) {
            val magnetRadius = 380f
            for (c in collectibles) {
                if (!c.isCollected) {
                    val dx = (playerX + playerWidth * 0.5f) - c.x
                    val dy = (playerY - playerHeight * 0.5f) - c.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < magnetRadius) {
                        c.x += (dx / dist) * 720f * dt
                        c.y += (dy / dist) * 720f * dt
                    }
                }
            }
        }

        // Move obstacles
        val obstacleIterator = obstacles.iterator()
        while (obstacleIterator.hasNext()) {
            val obs = obstacleIterator.next()
            obs.x -= currentSpeed * dt
            if (obs.x + obs.width < -100f) {
                obstacleIterator.remove()
            }
        }

        // Move collectibles
        val collectibleIterator = collectibles.iterator()
        while (collectibleIterator.hasNext()) {
            val col = collectibleIterator.next()
            col.x -= currentSpeed * dt
            if (col.x < -100f) {
                collectibleIterator.remove()
            }
        }

        // Move speech banners
        val bannerIterator = speechBanners.iterator()
        while (bannerIterator.hasNext()) {
            val banner = bannerIterator.next()
            banner.y -= 30f * dt
            banner.lifeSeconds -= dt
            banner.alpha = (banner.lifeSeconds / 2.2f).coerceIn(0f, 1f)
            if (banner.lifeSeconds <= 0f) {
                bannerIterator.remove()
            }
        }

        // Spawn new obstacles
        nextObstacleTimer -= dt
        if (nextObstacleTimer <= 0f) {
            spawnObstacle()
            nextObstacleTimer = Random.nextFloat() * 1.5f + 1.4f - (distanceMeters * 0.0004f).coerceAtMost(0.6f)
        }

        // Spawn new collectibles
        nextCollectibleTimer -= dt
        if (nextCollectibleTimer <= 0f) {
            spawnCollectibleCluster()
            nextCollectibleTimer = Random.nextFloat() * 1.2f + 0.8f
        }

        // Collision Checks with Collectibles
        checkCollectibleCollisions()

        // Collision Checks with Obstacles
        checkObstacleCollisions()
    }

    private fun checkCollectibleCollisions() {
        val effectiveHeight = if (isSliding) playerHeight * 0.45f else playerHeight
        val pLeft = playerX
        val pRight = playerX + playerWidth
        val pTop = playerY - effectiveHeight
        val pBottom = playerY

        for (c in collectibles) {
            if (c.isCollected) continue
            // Circle vs AABB bounding check
            val closestX = c.x.coerceIn(pLeft, pRight)
            val closestY = c.y.coerceIn(pTop, pBottom)
            val dx = c.x - closestX
            val dy = c.y - closestY
            if (dx * dx + dy * dy <= (c.radius + 10f) * (c.radius + 10f)) {
                c.isCollected = true
                applyCollectible(c.type)
            }
        }
    }

    private fun applyCollectible(type: CollectibleType) {
        val multiplier = if (isMultiplierActive) 2 else 1
        score += type.points * multiplier
        coinsCollected += type.coins

        when (type) {
            CollectibleType.VikasCoin -> {
                soundManager.playCoin()
                hapticManager.collect()
            }
            CollectibleType.SwachhBroom -> {
                broomsCollected++
                cleanCombo++
                maxCleanCombo = maxOf(maxCleanCombo, cleanCombo)
                activeMultiplierTime = 8.0f
                soundManager.playBroomClean()
                hapticManager.collect()
                addSpeechBanner("Clean India! +Combo 🧹")
            }
            CollectibleType.SolarSpark -> {
                score += 50
                soundManager.playCoin()
                hapticManager.collect()
            }
            CollectibleType.MakeInIndiaGear -> {
                score += 80
                soundManager.playCoin()
                hapticManager.collect()
            }
            CollectibleType.PowerUpShield -> {
                activeShieldTime = PowerUpType.MitronShield.durationSeconds
                powerUpsUsed++
                soundManager.playPowerUp()
                hapticManager.powerUp()
                addSpeechBanner("Mitron Shield Active! 🛡️")
            }
            CollectibleType.PowerUpTurbo -> {
                activeTurboTime = PowerUpType.VandeBharatTurbo.durationSeconds
                powerUpsUsed++
                soundManager.playPowerUp()
                hapticManager.powerUp()
                addSpeechBanner("Vande Bharat Turbo! 🚆")
            }
            CollectibleType.PowerUpMagnet -> {
                activeMagnetTime = PowerUpType.DigitalMagnet.durationSeconds
                powerUpsUsed++
                soundManager.playPowerUp()
                hapticManager.powerUp()
                addSpeechBanner("Digital Magnet! 🧲")
            }
        }
    }

    private fun checkObstacleCollisions() {
        val effectiveHeight = if (isSliding) playerHeight * 0.45f else playerHeight
        val pLeft = playerX + 12f
        val pRight = playerX + playerWidth - 12f
        val pTop = playerY - effectiveHeight
        val pBottom = playerY - 4f

        val it = obstacles.iterator()
        while (it.hasNext()) {
            val obs = it.next()
            val oLeft = obs.x
            val oRight = obs.x + obs.width
            val oTop = obs.y
            val oBottom = obs.y + obs.height

            // Bounding box overlap
            val overlap = pRight > oLeft && pLeft < oRight && pBottom > oTop && pTop < oBottom

            if (overlap) {
                if (isTurboActive) {
                    // Turbo destroys obstacles instantly!
                    it.remove()
                    score += 100
                    hapticManager.powerUp()
                    soundManager.playBroomClean()
                    addSpeechBanner("Obstacle Cleared! 💥")
                } else if (isShieldActive) {
                    // Shield absorbs hit
                    activeShieldTime = 0f
                    it.remove()
                    hapticManager.collision()
                    soundManager.playCrash()
                    addSpeechBanner("Shield Protected! 🛡️")
                } else {
                    // Crash! Game Over!
                    triggerGameOver()
                    break
                }
            }
        }
    }

    private fun triggerGameOver() {
        isGameOver = true
        isRunning = false
        soundManager.playCrash()
        hapticManager.collision()
        onGameOver(
            RunStats(
                score = score,
                distanceMeters = distanceMeters,
                coinsCollected = coinsCollected,
                broomsCollected = broomsCollected,
                maxCleanStreak = maxCleanCombo,
                powerUpsCollected = powerUpsUsed
            )
        )
    }

    private fun spawnObstacle() {
        val id = ++entityIdGen
        val spawnX = viewWidth + 80f
        val roll = Random.nextFloat()

        val obstacle = when {
            roll < 0.28f -> {
                // Low hurdle (Pothole / debris)
                RunObstacle(
                    id = id,
                    x = spawnX,
                    y = groundY - 48f,
                    width = 64f,
                    height = 48f,
                    type = ObstacleType.Pothole
                )
            }
            roll < 0.60f -> {
                // Red Tape Hurdle
                RunObstacle(
                    id = id,
                    x = spawnX,
                    y = groundY - 68f,
                    width = 54f,
                    height = 68f,
                    type = ObstacleType.RedTapeHurdle
                )
            }
            roll < 0.82f -> {
                // Corruption barrier (wider block)
                RunObstacle(
                    id = id,
                    x = spawnX,
                    y = groundY - 84f,
                    width = 72f,
                    height = 84f,
                    type = ObstacleType.CorruptionBlock
                )
            }
            else -> {
                // Overhead beam/banner requiring slide!
                RunObstacle(
                    id = id,
                    x = spawnX,
                    y = groundY - 145f,
                    width = 90f,
                    height = 70f,
                    type = ObstacleType.BureaucracyGate,
                    isOverhead = true
                )
            }
        }
        obstacles.add(obstacle)
    }

    private fun spawnCollectibleCluster() {
        val spawnX = viewWidth + 60f
        val clusterRoll = Random.nextFloat()

        if (clusterRoll < 0.65f) {
            // Arc of 3 to 5 coins
            val count = Random.nextInt(3, 6)
            val isHighArc = Random.nextBoolean()
            for (i in 0 until count) {
                val cx = spawnX + i * 55f
                val cy = if (isHighArc) {
                    groundY - 140f - sin((i.toFloat() / (count - 1)) * Math.PI.toFloat()) * 80f
                } else {
                    groundY - 35f
                }
                collectibles.add(
                    RunCollectible(
                        id = ++entityIdGen,
                        x = cx,
                        y = cy,
                        radius = 16f,
                        type = CollectibleType.VikasCoin
                    )
                )
            }
        } else if (clusterRoll < 0.85f) {
            // Swachh Broom or Solar Spark
            val isBroom = Random.nextBoolean()
            val type = if (isBroom) CollectibleType.SwachhBroom else CollectibleType.SolarSpark
            collectibles.add(
                RunCollectible(
                    id = ++entityIdGen,
                    x = spawnX,
                    y = groundY - 50f,
                    radius = 20f,
                    type = type
                )
            )
        } else {
            // Rare Power-Up
            val pRoll = Random.nextFloat()
            val powerType = when {
                pRoll < 0.35f -> CollectibleType.PowerUpTurbo
                pRoll < 0.70f -> CollectibleType.PowerUpShield
                else -> CollectibleType.PowerUpMagnet
            }
            collectibles.add(
                RunCollectible(
                    id = ++entityIdGen,
                    x = spawnX,
                    y = groundY - 110f,
                    radius = 24f,
                    type = powerType
                )
            )
        }
    }

    private fun triggerMilestoneSpeech(dist: Int) {
        val quotes = listOf(
            "Sabka Saath, Sabka Vikas!",
            "India is moving fast! 🇮🇳",
            "Swachh Bharat, Swasth Bharat!",
            "Digital Power to 1.4 Billion!",
            "Make in India, Make for World!",
            "Vande Bharat Speed! 🚆",
            "Viksit Bharat 2047!",
            "Yoga & Energy for Victory!"
        )
        val selected = quotes[Random.nextInt(quotes.size)]
        addSpeechBanner("$selected ($dist m)")
        soundManager.playRallyCheer()
    }

    fun addSpeechBanner(text: String) {
        speechBanners.add(
            FloatingSpeech(
                text = text,
                x = viewWidth * 0.5f,
                y = groundY - 260f,
                alpha = 1.0f,
                lifeSeconds = 2.4f
            )
        )
    }
}
