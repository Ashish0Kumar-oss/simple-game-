package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.components.ModiAvatar
import com.example.ui.components.ModiPose

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    val coins by viewModel.coins.collectAsState()
    val vikasPoints by viewModel.vikasPoints.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val bestDistance by viewModel.bestDistance.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_time"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF9933).copy(alpha = 0.22f),
                        Color(0xFFFFFFFF),
                        Color(0xFF138808).copy(alpha = 0.16f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Coins & Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coins & Points Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0D253F))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "🪙 $coins", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "⚡ $vikasPoints VP", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Audio & Haptics & Help Toggles
            Row {
                IconButton(
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier.testTag("toggle_sound_btn")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle Sound",
                        tint = if (soundEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleHaptics() },
                    modifier = Modifier.testTag("toggle_haptics_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Toggle Haptics",
                        tint = if (hapticsEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(
                    onClick = { viewModel.navigateTo(GameScreen.HowToPlay) },
                    modifier = Modifier.testTag("help_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "How To Play",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title Header with Tiranga Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF9933), Color(0xFFFFFFFF), Color(0xFF138808))
                    )
                )
                .padding(2.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0D233A),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🇮🇳 VIKAS YATRA 🇮🇳",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "MODI RUN",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFD84315),
            letterSpacing = 2.sp
        )

        Text(
            text = "Nation Building Adventure & Missions",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF455A64),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center Animated Avatar Hero Card
        Card(
            modifier = Modifier
                .size(160.dp)
                .shadow(12.dp, shape = CircleShape)
                .border(4.dp, Color(0xFFFF8F00), CircleShape),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ModiAvatar(
                    size = 145.dp,
                    pose = ModiPose.Standing,
                    animTime = animTime
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speech bubble
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0D253F),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "“Mitron! Let’s run together for Viksit Bharat!”",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Stats Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFFFE0B2), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "High Score", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = "$highScore", fontSize = 20.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0xFFFFCC80)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Best Distance", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = "$bestDistance m", fontSize = 20.sp, color = Color(0xFF1B8A2F), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Action: Big Start Run Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clickable { viewModel.navigateTo(GameScreen.VikasRun) }
                .testTag("start_run_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF6F00), Color(0xFFFF8F00), Color(0xFFE65100))
                        )
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🏃", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "START VIKAS RUN",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Jump, collect brooms, speed up!",
                                color = Color(0xFFFFE0B2),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(text = "▶", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of 3 Game Modes: Nation Builder, Chandrayaan 3, Cabinet Awards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Nation Builder Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clickable { viewModel.navigateTo(GameScreen.NationBuilder) }
                    .testTag("nation_builder_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🏛️", fontSize = 22.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "Nation Builder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "Upgrade schemes & GDP",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Chandrayaan 3 Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .clickable { viewModel.navigateTo(GameScreen.Chandrayaan) }
                    .testTag("chandrayaan_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B29)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x3300E5FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Moon Mission",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Chandrayaan 3",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Land Vikram on Moon",
                            fontSize = 11.sp,
                            color = Color(0xFFB0BEC5),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cabinet Awards Card (Full width)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clickable { viewModel.navigateTo(GameScreen.Achievements) }
                .testTag("cabinet_awards_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFF8E1),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Cabinet Trophies",
                                tint = Color(0xFFFFA000)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Cabinet Awards & Trophies",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                        Text(
                            text = "Unlock 8 national honors and earn coins",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                Text(text = "➜", color = Color(0xFFFF8F00), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}
