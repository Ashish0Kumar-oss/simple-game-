package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.model.RunStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Modi Run", appName)
    }

    @Test
    fun `game repository records run stats and updates coins`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)

        val initialCoins = repo.coins.value
        repo.recordRunResult(
            RunStats(
                score = 500,
                distanceMeters = 300,
                coinsCollected = 25,
                broomsCollected = 3,
                maxCleanStreak = 2,
                powerUpsCollected = 1
            )
        )

        assertEquals(initialCoins + 25L, repo.coins.value)
        assertTrue(repo.highScore.value >= 500)
        assertTrue(repo.bestDistance.value >= 300)
    }
}
