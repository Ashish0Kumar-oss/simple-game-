package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticManager
import com.example.audio.SoundManager
import com.example.data.GameRepository
import com.example.model.GameScreen
import com.example.model.RunStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val repository = GameRepository(application)
    val soundManager = SoundManager { repository.soundEnabled.value }
    val hapticManager = HapticManager(application) { repository.hapticsEnabled.value }

    private val _screenStack = MutableStateFlow(listOf(GameScreen.MainMenu))
    val currentScreen: StateFlow<GameScreen> = MutableStateFlow(GameScreen.MainMenu)

    val coins = repository.coins
    val vikasPoints = repository.vikasPoints
    val schemes = repository.schemes
    val highScore = repository.highScore
    val bestDistance = repository.bestDistance
    val soundEnabled = repository.soundEnabled
    val hapticsEnabled = repository.hapticsEnabled

    // Nation Builder Rally Fever (2x boost)
    private val _rallyFeverTime = MutableStateFlow(0)
    val rallyFeverTime: StateFlow<Int> = _rallyFeverTime.asStateFlow()

    // Last completed run result for dialog
    private val _lastRunStats = MutableStateFlow<RunStats?>(null)
    val lastRunStats: StateFlow<RunStats?> = _lastRunStats.asStateFlow()

    // Chandrayaan dialog state
    private val _chandrayaanResult = MutableStateFlow<Pair<Int, Boolean>?>(null)
    val chandrayaanResult: StateFlow<Pair<Int, Boolean>?> = _chandrayaanResult.asStateFlow()

    init {
        // Real-time Nation Builder GDP & Vikas Points idle ticker
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val baseVPS = schemes.value.sumOf { it.currentVPS }
                if (baseVPS > 0) {
                    val multiplier = if (_rallyFeverTime.value > 0) 2 else 1
                    val gained = baseVPS * multiplier
                    repository.addVikasPoints(gained)
                }

                if (_rallyFeverTime.value > 0) {
                    _rallyFeverTime.value = _rallyFeverTime.value - 1
                }
            }
        }
    }

    fun navigateTo(screen: GameScreen) {
        soundManager.playButtonClick()
        hapticManager.tick()
        val current = _screenStack.value
        _screenStack.value = current + screen
        (currentScreen as MutableStateFlow).value = screen
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        if (current.size > 1) {
            soundManager.playButtonClick()
            hapticManager.tick()
            val newStack = current.dropLast(1)
            _screenStack.value = newStack
            (currentScreen as MutableStateFlow).value = newStack.last()
            return true
        }
        return false
    }

    fun recordRunResult(stats: RunStats) {
        repository.recordRunResult(stats)
        _lastRunStats.value = stats
    }

    fun clearLastRunStats() {
        _lastRunStats.value = null
    }

    fun recordChandrayaanResult(score: Int, success: Boolean) {
        if (success) {
            repository.recordChandrayaanLanding(score)
        }
        _chandrayaanResult.value = Pair(score, success)
    }

    fun clearChandrayaanResult() {
        _chandrayaanResult.value = null
    }

    fun triggerRallySpeech() {
        soundManager.playRallyCheer()
        hapticManager.powerUp()
        _rallyFeverTime.value = 15
        repository.addCoins(5L)
        repository.addVikasPoints(25L)
    }

    fun upgradeScheme(schemeId: String) {
        val success = repository.upgradeScheme(schemeId)
        if (success) {
            soundManager.playCoin()
            hapticManager.collect()
        } else {
            soundManager.playCrash()
            hapticManager.tick()
        }
    }

    fun claimAchievement(id: String, rewardCoins: Int) {
        val success = repository.claimAchievement(id, rewardCoins)
        if (success) {
            soundManager.playPowerUp()
            hapticManager.powerUp()
        }
    }

    fun toggleSound() {
        val newSetting = !soundEnabled.value
        repository.setSoundEnabled(newSetting)
        if (newSetting) soundManager.playButtonClick()
    }

    fun toggleHaptics() {
        val newSetting = !hapticsEnabled.value
        repository.setHapticsEnabled(newSetting)
        if (newSetting) hapticManager.tick()
    }
}
