package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Achievement
import com.example.model.RunStats
import com.example.model.VikasScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("modi_game_prefs", Context.MODE_PRIVATE)

    private val _coins = MutableStateFlow(prefs.getLong(KEY_COINS, 100L))
    val coins: StateFlow<Long> = _coins.asStateFlow()

    private val _vikasPoints = MutableStateFlow(prefs.getLong(KEY_VIKAS_POINTS, 50L))
    val vikasPoints: StateFlow<Long> = _vikasPoints.asStateFlow()

    private val _highScore = MutableStateFlow(prefs.getInt(KEY_HIGH_SCORE, 0))
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _bestDistance = MutableStateFlow(prefs.getInt(KEY_BEST_DISTANCE, 0))
    val bestDistance: StateFlow<Int> = _bestDistance.asStateFlow()

    private val _totalRuns = MutableStateFlow(prefs.getInt(KEY_TOTAL_RUNS, 0))
    val totalRuns: StateFlow<Int> = _totalRuns.asStateFlow()

    private val _totalBrooms = MutableStateFlow(prefs.getInt(KEY_TOTAL_BROOMS, 0))
    val totalBrooms: StateFlow<Int> = _totalBrooms.asStateFlow()

    private val _chandrayaanLandings = MutableStateFlow(prefs.getInt(KEY_CHANDRAYAAN_LANDINGS, 0))
    val chandrayaanLandings: StateFlow<Int> = _chandrayaanLandings.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS, true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _schemes = MutableStateFlow(loadInitialSchemes())
    val schemes: StateFlow<List<VikasScheme>> = _schemes.asStateFlow()

    private val _claimedAchievements = MutableStateFlow(loadClaimedAchievements())
    val claimedAchievements: StateFlow<Set<String>> = _claimedAchievements.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }

    fun addCoins(amount: Long) {
        val updated = (_coins.value + amount).coerceAtLeast(0L)
        _coins.value = updated
        prefs.edit().putLong(KEY_COINS, updated).apply()
    }

    fun spendCoins(amount: Long): Boolean {
        if (_coins.value >= amount) {
            val updated = _coins.value - amount
            _coins.value = updated
            prefs.edit().putLong(KEY_COINS, updated).apply()
            return true
        }
        return false
    }

    fun addVikasPoints(amount: Long) {
        val updated = (_vikasPoints.value + amount).coerceAtLeast(0L)
        _vikasPoints.value = updated
        prefs.edit().putLong(KEY_VIKAS_POINTS, updated).apply()
    }

    fun spendVikasPoints(amount: Long): Boolean {
        if (_vikasPoints.value >= amount) {
            val updated = _vikasPoints.value - amount
            _vikasPoints.value = updated
            prefs.edit().putLong(KEY_VIKAS_POINTS, updated).apply()
            return true
        }
        return false
    }

    fun recordRunResult(stats: RunStats) {
        val newHighScore = maxOf(_highScore.value, stats.score)
        val newBestDistance = maxOf(_bestDistance.value, stats.distanceMeters)
        val updatedCoins = _coins.value + stats.coinsCollected
        val updatedRuns = _totalRuns.value + 1
        val updatedBrooms = _totalBrooms.value + stats.broomsCollected
        val pointsEarned = stats.score.toLong() / 2

        _highScore.value = newHighScore
        _bestDistance.value = newBestDistance
        _coins.value = updatedCoins
        _totalRuns.value = updatedRuns
        _totalBrooms.value = updatedBrooms
        _vikasPoints.value = _vikasPoints.value + pointsEarned

        prefs.edit()
            .putInt(KEY_HIGH_SCORE, newHighScore)
            .putInt(KEY_BEST_DISTANCE, newBestDistance)
            .putLong(KEY_COINS, updatedCoins)
            .putInt(KEY_TOTAL_RUNS, updatedRuns)
            .putInt(KEY_TOTAL_BROOMS, updatedBrooms)
            .putLong(KEY_VIKAS_POINTS, _vikasPoints.value)
            .apply()
    }

    fun recordChandrayaanLanding(scoreBonus: Int) {
        val landings = _chandrayaanLandings.value + 1
        val coinsReward = 50L + scoreBonus / 10
        val vikasReward = 500L

        _chandrayaanLandings.value = landings
        _coins.value += coinsReward
        _vikasPoints.value += vikasReward

        prefs.edit()
            .putInt(KEY_CHANDRAYAAN_LANDINGS, landings)
            .putLong(KEY_COINS, _coins.value)
            .putLong(KEY_VIKAS_POINTS, _vikasPoints.value)
            .apply()
    }

    fun upgradeScheme(schemeId: String): Boolean {
        val currentList = _schemes.value
        val scheme = currentList.find { it.id == schemeId } ?: return false
        val cost = scheme.currentCost

        if (spendVikasPoints(cost)) {
            val updatedList = currentList.map {
                if (it.id == schemeId) it.copy(level = it.level + 1) else it
            }
            _schemes.value = updatedList
            saveSchemeLevel(schemeId, scheme.level + 1)
            return true
        }
        return false
    }

    fun claimAchievement(id: String, rewardCoins: Int): Boolean {
        if (!_claimedAchievements.value.contains(id)) {
            val updated = _claimedAchievements.value + id
            _claimedAchievements.value = updated
            addCoins(rewardCoins.toLong())
            prefs.edit().putStringSet(KEY_CLAIMED_ACHIEVEMENTS, updated).apply()
            return true
        }
        return false
    }

    fun getAchievements(): List<Achievement> {
        val claimed = _claimedAchievements.value
        val totalBroomsCount = _totalBrooms.value.toLong()
        val highDist = _bestDistance.value.toLong()
        val score = _highScore.value.toLong()
        val landings = _chandrayaanLandings.value.toLong()
        val runs = _totalRuns.value.toLong()
        val totalVPS = _schemes.value.sumOf { it.currentVPS }

        return listOf(
            Achievement(
                id = "ach_first_steps",
                title = "Vikas Yatri",
                description = "Run your first 100 meters in Vikas Run",
                iconEmoji = "🏃",
                target = 100L,
                current = highDist,
                rewardCoins = 30,
                isClaimed = claimed.contains("ach_first_steps")
            ),
            Achievement(
                id = "ach_swachh_bharat",
                title = "Swachh Bharat Hero",
                description = "Collect 20 Clean Brooms across all runs",
                iconEmoji = "🧹",
                target = 20L,
                current = totalBroomsCount,
                rewardCoins = 50,
                isClaimed = claimed.contains("ach_swachh_bharat")
            ),
            Achievement(
                id = "ach_distance_runner",
                title = "National Expressway",
                description = "Reach 500 meters in a single run",
                iconEmoji = "🛣️",
                target = 500L,
                current = highDist,
                rewardCoins = 80,
                isClaimed = claimed.contains("ach_distance_runner")
            ),
            Achievement(
                id = "ach_score_master",
                title = "Vikas Visionary",
                description = "Achieve a score of 1,000 points in Vikas Run",
                iconEmoji = "✨",
                target = 1000L,
                current = score,
                rewardCoins = 100,
                isClaimed = claimed.contains("ach_score_master")
            ),
            Achievement(
                id = "ach_chandrayaan",
                title = "Shiv Shakti Point",
                description = "Land Chandrayaan 3 safely on Moon South Pole",
                iconEmoji = "🚀",
                target = 1L,
                current = landings,
                rewardCoins = 150,
                isClaimed = claimed.contains("ach_chandrayaan")
            ),
            Achievement(
                id = "ach_gdp_growth",
                title = "Economic Powerhouse",
                description = "Generate 100 Vikas Points per second in Nation Builder",
                iconEmoji = "📈",
                target = 100L,
                current = totalVPS,
                rewardCoins = 120,
                isClaimed = claimed.contains("ach_gdp_growth")
            ),
            Achievement(
                id = "ach_dedicated_patriot",
                title = "Sabka Prayas",
                description = "Complete 5 Vikas Run missions",
                iconEmoji = "🇮🇳",
                target = 5L,
                current = runs,
                rewardCoins = 60,
                isClaimed = claimed.contains("ach_dedicated_patriot")
            ),
            Achievement(
                id = "ach_viksit_bharat",
                title = "Viksit Bharat 2047",
                description = "Reach 1,500 meters distance in Vikas Run",
                iconEmoji = "🏆",
                target = 1500L,
                current = highDist,
                rewardCoins = 250,
                isClaimed = claimed.contains("ach_viksit_bharat")
            )
        )
    }

    private fun loadInitialSchemes(): List<VikasScheme> {
        val schemesConfig = listOf(
            VikasScheme("swachh", "Swachh Bharat Abhiyan", "स्वच्छ भारत", "Clean cities & zero waste", "🧹", 40L, 2L),
            VikasScheme("surya_ghar", "PM Surya Ghar", "सूर्य घर योजना", "Rooftop solar energy for homes", "☀️", 120L, 6L),
            VikasScheme("vande_bharat", "Vande Bharat Express", "वंदे भारत", "High speed train connectivity", "🚆", 350L, 18L),
            VikasScheme("digital_india", "Digital India & UPI", "डिजिटल भारत", "Instant cashless digital economy", "📱", 900L, 45L),
            VikasScheme("make_in_india", "Make in India", "मेक इन इंडिया", "Global manufacturing & chip fabs", "🏭", 2400L, 120L),
            VikasScheme("isro_space", "ISRO Space Missions", "इसरो अंतरिक्ष", "Moon & Mars planetary exploration", "🚀", 6000L, 300L),
            VikasScheme("bharatmala", "Bharatmala Expressways", "भारतमाला हाईवे", "Modern high speed corridor network", "🛣️", 15000L, 750L),
            VikasScheme("ayushman", "Ayushman Bharat", "आयुष्मान भारत", "Universal healthcare shield for all", "🏥", 40000L, 2000L)
        )

        return schemesConfig.map {
            val level = prefs.getInt("scheme_level_${it.id}", 0)
            it.copy(level = level)
        }
    }

    private fun saveSchemeLevel(schemeId: String, level: Int) {
        prefs.edit().putInt("scheme_level_$schemeId", level).apply()
    }

    private fun loadClaimedAchievements(): Set<String> {
        return prefs.getStringSet(KEY_CLAIMED_ACHIEVEMENTS, emptySet()) ?: emptySet()
    }

    companion object {
        private const val KEY_COINS = "key_coins"
        private const val KEY_VIKAS_POINTS = "key_vikas_points"
        private const val KEY_HIGH_SCORE = "key_high_score"
        private const val KEY_BEST_DISTANCE = "key_best_distance"
        private const val KEY_TOTAL_RUNS = "key_total_runs"
        private const val KEY_TOTAL_BROOMS = "key_total_brooms"
        private const val KEY_CHANDRAYAAN_LANDINGS = "key_chandrayaan_landings"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_HAPTICS = "key_haptics"
        private const val KEY_CLAIMED_ACHIEVEMENTS = "key_claimed_achievements"
    }
}
