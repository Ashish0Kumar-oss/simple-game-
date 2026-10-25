package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel

@Composable
fun HowToPlayScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F7F2))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.testTag("back_from_how_to_play")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0D233A)
                )
            }
            Text(
                text = "HOW TO PLAY VIKAS YATRA",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD84315),
                letterSpacing = 1.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Vikas Runner Controls
            GuideSectionCard(
                title = "🏃 Vikas Runner Controls",
                color = Color(0xFFD84315)
            ) {
                GuideItem(
                    emoji = "⬆️",
                    title = "Tap to Jump",
                    desc = "Tap screen or JUMP button to leap over potholes, corruption barriers, and red tape."
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "⏫",
                    title = "Double Jump",
                    desc = "Tap again in mid-air to perform an acrobatic super jump for tall obstacles."
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "⬇️",
                    title = "Slide / Duck",
                    desc = "Tap SLIDE button to duck underneath high overhead bureaucracy beams."
                )
            }

            // Section 2: Items & Power-ups
            GuideSectionCard(
                title = "⚡ Collectibles & Power-ups",
                color = Color(0xFF1B8A2F)
            ) {
                GuideItem(
                    emoji = "🪙",
                    title = "Vikas Coins",
                    desc = "Collect golden coins to spend on upgrades and achievements."
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "🧹",
                    title = "Swachh Bharat Broom",
                    desc = "Clean up the streets and activate a 2X Clean Streak score multiplier!"
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "🚆",
                    title = "Vande Bharat Turbo",
                    desc = "Supercharge Modi into high-speed train mode! Smashes all obstacles in your path."
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "🛡️",
                    title = "Mitron Shield",
                    desc = "Grants a protective aura that deflects an obstacle collision."
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "🧲",
                    title = "Digital India Magnet",
                    desc = "Draws all nearby coins towards you automatically."
                )
            }

            // Section 3: Nation Builder & Chandrayaan
            GuideSectionCard(
                title = "🏛️ Nation Builder & Moon Landing",
                color = Color(0xFF0D253F)
            ) {
                GuideItem(
                    emoji = "📈",
                    title = "Grow Indian GDP",
                    desc = "Invest your Vikas Points into 8 national schemes to reach $10T Viksit Bharat 2047."
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "📢",
                    title = "Mitron! Rally Speech",
                    desc = "Tap the speech rally card to trigger 2X Vikas Fever output for 15 seconds!"
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideItem(
                    emoji = "🚀",
                    title = "Chandrayaan 3 Mission",
                    desc = "Pilot Vikram lander using thrusters. Touch down softly on Shiv Shakti Point to deploy Pragyan Rover!"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GuideSectionCard(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun GuideItem(emoji: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFFF3E0),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0D233A))
            Text(text = desc, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
        }
    }
}
