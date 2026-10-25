package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.ChandrayaanScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.NationBuilderScreen
import com.example.ui.screens.VikasRunScreen
import com.example.ui.theme.ModiGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModiGameTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ModiGameApp()
                }
            }
        }
    }
}

@Composable
fun ModiGameApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            GameScreen.MainMenu -> MainMenuScreen(viewModel = viewModel)
            GameScreen.VikasRun -> VikasRunScreen(viewModel = viewModel)
            GameScreen.NationBuilder -> NationBuilderScreen(viewModel = viewModel)
            GameScreen.Chandrayaan -> ChandrayaanScreen(viewModel = viewModel)
            GameScreen.Achievements -> AchievementsScreen(viewModel = viewModel)
            GameScreen.HowToPlay -> HowToPlayScreen(viewModel = viewModel)
        }
    }
}
