package com.example.model

enum class GameScreen {
    MainMenu,
    VikasRun,
    NationBuilder,
    Chandrayaan,
    Achievements,
    HowToPlay
}

enum class PowerUpType(val displayName: String, val durationSeconds: Float) {
    VandeBharatTurbo("Vande Bharat Turbo 🚆", 7.0f),
    MitronShield("Mitron Shield 🛡️", 10.0f),
    DigitalMagnet("Digital India Magnet 🧲", 9.0f),
    SwachhMultiplier("Swachh Clean Streak 🧹", 8.0f)
}

enum class ObstacleType(val displayName: String) {
    RedTapeHurdle("Red Tape"),
    Pothole("Road Pothole"),
    CorruptionBlock("Corruption Barrier"),
    BureaucracyGate("Speed Breaker")
}

enum class CollectibleType(val points: Int, val coins: Int) {
    VikasCoin(points = 10, coins = 1),
    SwachhBroom(points = 25, coins = 3),
    SolarSpark(points = 30, coins = 4),
    MakeInIndiaGear(points = 50, coins = 5),
    PowerUpShield(points = 100, coins = 10),
    PowerUpTurbo(points = 150, coins = 15),
    PowerUpMagnet(points = 100, coins = 10)
}

data class VikasScheme(
    val id: String,
    val name: String,
    val hindiName: String,
    val tagLine: String,
    val iconEmoji: String,
    val baseCost: Long,
    val baseVPS: Long, // Vikas Points per second
    val level: Int = 0
) {
    val currentCost: Long
        get() = (baseCost * Math.pow(1.22, level.toDouble())).toLong().coerceAtLeast(baseCost)

    val currentVPS: Long
        get() = (baseVPS * level * (1.0 + (level / 10.0) * 0.15)).toLong()
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val target: Long,
    val current: Long,
    val rewardCoins: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = current >= target
    val progressFraction: Float get() = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
}

data class RunStats(
    val score: Int = 0,
    val distanceMeters: Int = 0,
    val coinsCollected: Int = 0,
    val broomsCollected: Int = 0,
    val maxCleanStreak: Int = 0,
    val powerUpsCollected: Int = 0
)
