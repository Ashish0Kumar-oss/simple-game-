package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.RunCollectible
import com.example.game.RunObstacle
import com.example.game.VikasRunEngine
import com.example.model.CollectibleType
import com.example.model.GameScreen
import com.example.model.ObstacleType
import com.example.model.PowerUpType
import com.example.model.RunStats
import com.example.ui.GameViewModel
import com.example.ui.components.ModiPose
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VikasRunScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    var gameOverStats by remember { mutableStateOf<RunStats?>(null) }

    val engine = remember {
        VikasRunEngine(
            soundManager = viewModel.soundManager,
            hapticManager = viewModel.hapticManager,
            onGameOver = { stats ->
                gameOverStats = stats
                viewModel.recordRunResult(stats)
            }
        )
    }

    var initialized by remember { mutableStateOf(false) }
    var currentFrameTimeNanos by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        LaunchedEffect(widthPx, heightPx) {
            if (widthPx > 0f && heightPx > 0f && !initialized) {
                engine.reset(widthPx, heightPx)
                initialized = true
            }
        }

        // Game Loop at native frame rate
        LaunchedEffect(initialized) {
            if (!initialized) return@LaunchedEffect
            var lastFrameTime = 0L
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTime != 0L) {
                        val dt = ((frameTimeNanos - lastFrameTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        engine.update(dt)
                        currentFrameTimeNanos = frameTimeNanos.toFloat()
                    }
                    lastFrameTime = frameTimeNanos
                }
            }
        }

        // Tap area for game interaction: Left bottom area for slide, everything else jumps!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (!engine.isGameOver && !engine.isPaused) {
                                if (offset.y > heightPx * 0.72f && offset.x < widthPx * 0.45f) {
                                    engine.slide()
                                } else {
                                    engine.jump()
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Trigger recomposition on each frame
                @Suppress("UNUSED_VARIABLE")
                val tick = currentFrameTimeNanos

                drawParallaxBackground(size.width, size.height, engine.distanceMeters, engine.groundY)
                drawTrackAndGround(size.width, size.height, engine.groundY, engine.distanceMeters, engine.isTurboActive)
                drawObstacles(engine.obstacles)
                drawCollectibles(engine.collectibles, engine.animTime)
                drawModiPlayer(engine)
                drawSpeechBanners(engine.speechBanners)

                // Speed lines if Turbo active
                if (engine.isTurboActive) {
                    drawSpeedLines(size.width, size.height, engine.animTime)
                }
            }
        }

        // HUD Overlay (Top)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score and Distance Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xCC0D253F),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${engine.distanceMeters}m",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Score: ${engine.score}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Coins and Multiplier
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xCC0D253F),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🪙 ${engine.coinsCollected}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (engine.broomsCollected > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🧹 ${engine.broomsCollected}",
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Pause Button
                    IconButton(
                        onClick = { engine.isPaused = !engine.isPaused },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0D253F))
                            .testTag("pause_game_btn")
                    ) {
                        Icon(
                            imageVector = if (engine.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Power-up Timers Bar
            if (engine.isTurboActive || engine.isShieldActive || engine.isMagnetActive || engine.isMultiplierActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (engine.isTurboActive) {
                        PowerUpPill(
                            label = "🚆 Turbo",
                            progress = engine.activeTurboTime / PowerUpType.VandeBharatTurbo.durationSeconds,
                            color = Color(0xFFFF6F00)
                        )
                    }
                    if (engine.isShieldActive) {
                        PowerUpPill(
                            label = "🛡️ Shield",
                            progress = engine.activeShieldTime / PowerUpType.MitronShield.durationSeconds,
                            color = Color(0xFF00E5FF)
                        )
                    }
                    if (engine.isMagnetActive) {
                        PowerUpPill(
                            label = "🧲 Magnet",
                            progress = engine.activeMagnetTime / PowerUpType.DigitalMagnet.durationSeconds,
                            color = Color(0xFFE040FB)
                        )
                    }
                    if (engine.isMultiplierActive) {
                        PowerUpPill(
                            label = "🧹 2x Clean",
                            progress = engine.activeMultiplierTime / PowerUpType.SwachhMultiplier.durationSeconds,
                            color = Color(0xFF76FF03)
                        )
                    }
                }
            }
        }

        // On-Screen Touch Controls (Bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slide / Duck Button
            Surface(
                shape = CircleShape,
                color = if (engine.isSliding) Color(0xFFFF7A00) else Color(0xAA0D233A),
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFFF9933), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { engine.slide() })
                    }
                    .testTag("slide_btn")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Slide",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("SLIDE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            // Jump / Double Jump Button
            Surface(
                shape = CircleShape,
                color = Color(0xCCFF6F00),
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFFFD54F), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { engine.jump() })
                    }
                    .testTag("jump_btn")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Jump",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        if (engine.jumpCount == 1) "2X JUMP" else "JUMP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // Pause Overlay
        if (engine.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Yatra Paused",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD84315)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { engine.isPaused = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("resume_btn")
                        ) {
                            Text("Resume Yatra", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.navigateBack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D253F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("exit_run_btn")
                        ) {
                            Text("Main Menu")
                        }
                    }
                }
            }
        }

        // Game Over Dialog Modal
        AnimatedVisibility(
            visible = gameOverStats != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            gameOverStats?.let { stats ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(3.dp, Color(0xFFFF8F00), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🇮🇳 YATRA COMPLETE! 🇮🇳",
                                color = Color(0xFFD84315),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sabka Saath, Sabka Vikas!",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Score & Distance Highlight
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0D253F))
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                                    Text("${stats.score}", color = Color(0xFFFFD54F), fontSize = 24.sp, fontWeight = FontWeight.Black)
                                }
                                Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.3f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Distance", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                                    Text("${stats.distanceMeters} m", color = Color(0xFF81C784), fontSize = 24.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Detailed stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coins Collected:", fontSize = 13.sp, color = Color.DarkGray)
                                Text("🪙 ${stats.coinsCollected}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Swachh Brooms Swept:", fontSize = 13.sp, color = Color.DarkGray)
                                Text("🧹 ${stats.broomsCollected}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B8A2F))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vikas Points Added:", fontSize = 13.sp, color = Color.DarkGray)
                                Text("+${stats.score / 2} VP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Buttons
                            Button(
                                onClick = {
                                    gameOverStats = null
                                    engine.reset(widthPx, heightPx)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("run_again_btn")
                            ) {
                                Text("RUN AGAIN 🏃", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    gameOverStats = null
                                    viewModel.navigateBack()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D253F)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("menu_from_gameover_btn")
                            ) {
                                Text("Main Menu")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerUpPill(label: String, progress: Float, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD0D253F),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = color,
                trackColor = Color.DarkGray,
                modifier = Modifier
                    .width(64.dp)
                    .height(4.dp)
                    .clip(CircleShape)
            )
        }
    }
}

// Canvas Drawing Routines

private fun DrawScope.drawParallaxBackground(w: Float, h: Float, distMeters: Int, groundY: Float) {
    // Dynamic Sky Gradient (sunrise/morning patriotic sky)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFB300),
                Color(0xFFFFE0B2),
                Color(0xFFE1F5FE),
                Color(0xFFB3E5FC)
            ),
            startY = 0f,
            endY = groundY
        )
    )

    // Radiant Sun
    val sunX = w * 0.82f
    val sunY = h * 0.16f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0x66FFA726), Color.Transparent),
            center = Offset(sunX, sunY),
            radius = 120f
        ),
        radius = 120f,
        center = Offset(sunX, sunY)
    )
    drawCircle(color = Color(0xFFFFF176), radius = 32f, center = Offset(sunX, sunY))

    // Parallax Layer 1: Snow-capped Himalayas ridges (slow scroll)
    val mountainOffset = (distMeters * 0.8f) % w
    drawHimalayas(w, groundY - 140f, mountainOffset)

    // Parallax Layer 2: Monuments & Smart City skyline (medium scroll)
    val cityOffset = (distMeters * 2.2f) % (w * 1.5f)
    drawIndianCitySkyline(w, groundY, cityOffset)
}

private fun DrawScope.drawHimalayas(w: Float, baseY: Float, offset: Float) {
    val peakPath = Path().apply {
        moveTo(-w + offset, baseY)
        lineTo(-w * 0.7f + offset, baseY - 160f)
        lineTo(-w * 0.4f + offset, baseY - 60f)
        lineTo(-w * 0.1f + offset, baseY - 200f)
        lineTo(w * 0.3f + offset, baseY - 80f)
        lineTo(w * 0.6f + offset, baseY - 220f)
        lineTo(w * 0.9f + offset, baseY - 90f)
        lineTo(w * 1.2f + offset, baseY - 190f)
        lineTo(w * 1.5f + offset, baseY - 50f)
        lineTo(w * 1.8f + offset, baseY - 210f)
        lineTo(w * 2.2f + offset, baseY)
        close()
    }
    drawPath(
        peakPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFCFD8DC), Color(0xFF90A4AE)),
            startY = baseY - 220f,
            endY = baseY
        )
    )
}

private fun DrawScope.drawIndianCitySkyline(w: Float, groundY: Float, offset: Float) {
    val baseY = groundY - 10f
    // Modern towers, Red Fort ramparts, and solar panel arrays
    val startX = -offset
    for (i in 0..6) {
        val x = startX + i * (w * 0.35f)
        // Red Fort Dome / Gate silhouette
        if (i % 2 == 0) {
            drawRoundRect(
                color = Color(0x66BF360C),
                topLeft = Offset(x, baseY - 110f),
                size = Size(80f, 110f),
                cornerRadius = CornerRadius(10f)
            )
            // Dome
            drawArc(
                color = Color(0x66BF360C),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(x + 10f, baseY - 150f),
                size = Size(60f, 60f)
            )
            // Flag on pole
            drawLine(
                color = Color(0x88D84315),
                start = Offset(x + 40f, baseY - 150f),
                end = Offset(x + 40f, baseY - 178f),
                strokeWidth = 2f
            )
            drawRect(
                color = Color(0xFFFF9933),
                topLeft = Offset(x + 40f, baseY - 178f),
                size = Size(16f, 10f)
            )
        } else {
            // Modern High-tech IT / Smart City skyscraper
            drawRect(
                color = Color(0x55455A64),
                topLeft = Offset(x, baseY - 150f),
                size = Size(65f, 150f)
            )
            // Antenna
            drawLine(
                color = Color(0x55455A64),
                start = Offset(x + 32f, baseY - 150f),
                end = Offset(x + 32f, baseY - 180f),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawTrackAndGround(w: Float, h: Float, groundY: Float, distMeters: Int, isTurbo: Boolean) {
    // Ground base
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF37474F),
                Color(0xFF263238),
                Color(0xFF1B5E20) // India Green grass lower tier
            ),
            startY = groundY,
            endY = h
        ),
        topLeft = Offset(0f, groundY),
        size = Size(w, h - groundY)
    )

    // High Speed Track Rails (Vande Bharat railway)
    val railY1 = groundY + 12f
    val railY2 = groundY + 36f

    // Ties / Sleepers (scrolling fast)
    val tieSpacing = 42f
    val tieScroll = (distMeters * 6.5f) % tieSpacing
    var tx = -tieScroll
    while (tx < w + 50f) {
        drawRoundRect(
            color = Color(0xFF616161),
            topLeft = Offset(tx, railY1 - 4f),
            size = Size(14f, 34f),
            cornerRadius = CornerRadius(2f)
        )
        tx += tieSpacing
    }

    // Steel rails
    val railColor = if (isTurbo) Color(0xFFFFD54F) else Color(0xFFCFD8DC)
    drawLine(
        color = railColor,
        start = Offset(0f, railY1),
        end = Offset(w, railY1),
        strokeWidth = 5f
    )
    drawLine(
        color = railColor,
        start = Offset(0f, railY2),
        end = Offset(w, railY2),
        strokeWidth = 5f
    )

    // Road dashed line
    val roadDashScroll = (distMeters * 8f) % 60f
    var rx = -roadDashScroll
    while (rx < w + 60f) {
        drawRoundRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(rx, groundY + 62f),
            size = Size(36f, 6f),
            cornerRadius = CornerRadius(3f)
        )
        rx += 60f
    }
}

private fun DrawScope.drawObstacles(obstacles: List<RunObstacle>) {
    for (obs in obstacles) {
        when (obs.type) {
            ObstacleType.Pothole -> {
                // Damaged road pothole with warning cone
                drawOval(
                    color = Color(0xFF1E1E1E),
                    topLeft = Offset(obs.x, obs.y + obs.height - 18f),
                    size = Size(obs.width, 18f)
                )
                // Orange hazard cone
                val conePath = Path().apply {
                    moveTo(obs.x + obs.width * 0.5f, obs.y)
                    lineTo(obs.x + obs.width * 0.2f, obs.y + obs.height - 4f)
                    lineTo(obs.x + obs.width * 0.8f, obs.y + obs.height - 4f)
                    close()
                }
                drawPath(conePath, color = Color(0xFFFF5722))
                // White reflective stripe on cone
                drawRect(
                    color = Color.White,
                    topLeft = Offset(obs.x + obs.width * 0.35f, obs.y + obs.height * 0.45f),
                    size = Size(obs.width * 0.3f, 6f)
                )
            }
            ObstacleType.RedTapeHurdle -> {
                // Hurdle posts
                drawRect(color = Color(0xFF8D6E63), topLeft = Offset(obs.x + 4f, obs.y), size = Size(8f, obs.height))
                drawRect(color = Color(0xFF8D6E63), topLeft = Offset(obs.x + obs.width - 12f, obs.y), size = Size(8f, obs.height))
                // Red Tape barrier ribbons
                drawRect(color = Color(0xFFD32F2F), topLeft = Offset(obs.x, obs.y + 12f), size = Size(obs.width, 14f))
                drawRect(color = Color(0xFFD32F2F), topLeft = Offset(obs.x, obs.y + 36f), size = Size(obs.width, 14f))
                // Text symbol "RED TAPE"
                drawLine(color = Color.White, start = Offset(obs.x + 6f, obs.y + 19f), end = Offset(obs.x + obs.width - 6f, obs.y + 19f), strokeWidth = 2f)
            }
            ObstacleType.CorruptionBlock -> {
                // Heavy grey stone with padlock
                drawRoundRect(
                    color = Color(0xFF37474F),
                    topLeft = Offset(obs.x, obs.y),
                    size = Size(obs.width, obs.height),
                    cornerRadius = CornerRadius(8f)
                )
                // Warning stripes
                for (k in 0..2) {
                    drawLine(
                        color = Color(0xFFFFD54F),
                        start = Offset(obs.x + 10f + k * 18f, obs.y + 10f),
                        end = Offset(obs.x + 20f + k * 18f, obs.y + obs.height - 10f),
                        strokeWidth = 4f
                    )
                }
            }
            ObstacleType.BureaucracyGate -> {
                // Overhead beam requiring slide!
                drawRoundRect(
                    color = Color(0xFFB71C1C),
                    topLeft = Offset(obs.x, obs.y),
                    size = Size(obs.width, obs.height * 0.55f),
                    cornerRadius = CornerRadius(6f)
                )
                // Downward warning arrows
                drawLine(
                    color = Color.White,
                    start = Offset(obs.x + obs.width * 0.5f, obs.y + 6f),
                    end = Offset(obs.x + obs.width * 0.5f, obs.y + obs.height * 0.45f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.drawCollectibles(collectibles: List<RunCollectible>, animTime: Float) {
    for (col in collectibles) {
        if (col.isCollected) continue

        val bobY = sin(animTime * 8f + col.id) * 6f
        val cy = col.y + bobY
        val cx = col.x

        when (col.type) {
            CollectibleType.VikasCoin -> {
                // Golden Coin
                drawCircle(color = Color(0xFFFFB300), radius = col.radius, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFE082), radius = col.radius * 0.72f, center = Offset(cx, cy))
                // Rupee symbol (₹)
                drawLine(
                    color = Color(0xFFE65100),
                    start = Offset(cx - 5f, cy - 5f),
                    end = Offset(cx + 5f, cy - 5f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFFE65100),
                    start = Offset(cx - 5f, cy - 1f),
                    end = Offset(cx + 4f, cy - 1f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFFE65100),
                    start = Offset(cx - 2f, cy - 1f),
                    end = Offset(cx + 4f, cy + 6f),
                    strokeWidth = 2f
                )
            }
            CollectibleType.SwachhBroom -> {
                // Swachh Broom
                drawCircle(color = Color(0x334CAF50), radius = col.radius + 6f, center = Offset(cx, cy))
                // Broom handle (bamboo)
                drawLine(
                    color = Color(0xFF8D6E63),
                    start = Offset(cx - 10f, cy - 12f),
                    end = Offset(cx + 4f, cy + 4f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                // Bristles fan
                val broomPath = Path().apply {
                    moveTo(cx + 2f, cy + 2f)
                    lineTo(cx + 14f, cy + 10f)
                    lineTo(cx + 8f, cy + 16f)
                    close()
                }
                drawPath(broomPath, color = Color(0xFFFDD835))
            }
            CollectibleType.SolarSpark -> {
                // Bright solar spark
                drawCircle(color = Color(0xFFFF9100), radius = col.radius, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFFDE7), radius = col.radius * 0.5f, center = Offset(cx, cy))
            }
            CollectibleType.MakeInIndiaGear -> {
                // Make in India Gear
                drawCircle(color = Color(0xFF455A64), radius = col.radius, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFFFFF), radius = col.radius * 0.35f, center = Offset(cx, cy))
            }
            CollectibleType.PowerUpShield -> {
                // Cyan shield power-up
                drawCircle(color = Color(0xFF00E5FF), radius = col.radius, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = col.radius * 0.6f, center = Offset(cx, cy))
            }
            CollectibleType.PowerUpTurbo -> {
                // Vande Bharat Turbo badge
                drawCircle(color = Color(0xFFFF6F00), radius = col.radius, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFD54F), radius = col.radius * 0.65f, center = Offset(cx, cy))
            }
            CollectibleType.PowerUpMagnet -> {
                // Magnet badge
                drawCircle(color = Color(0xFFE040FB), radius = col.radius, center = Offset(cx, cy))
                drawCircle(color = Color.White, radius = col.radius * 0.6f, center = Offset(cx, cy))
            }
        }
    }
}

private fun DrawScope.drawModiPlayer(engine: VikasRunEngine) {
    val pose = when {
        engine.isTurboActive -> ModiPose.Turbo
        engine.isSliding -> ModiPose.Sliding
        !engine.isGrounded -> ModiPose.Jumping
        else -> ModiPose.Running
    }

    val px = engine.playerX
    val py = engine.playerY

    // If Turbo is active, draw trailing shadow copies for motion blur
    if (engine.isTurboActive) {
        drawCircle(
            color = Color(0x44FF9933),
            radius = 48f,
            center = Offset(px - 30f, py - 50f)
        )
        drawCircle(
            color = Color(0x33FF6F00),
            radius = 44f,
            center = Offset(px - 60f, py - 50f)
        )
    }

    // Shield bubble
    if (engine.isShieldActive) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x2200E5FF), Color(0x66FFD700), Color(0x9900E5FF)),
                center = Offset(px + engine.playerWidth * 0.5f, py - engine.playerHeight * 0.5f),
                radius = 70f
            ),
            radius = 66f,
            center = Offset(px + engine.playerWidth * 0.5f, py - engine.playerHeight * 0.5f)
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            style = Stroke(width = 3f),
            radius = 66f,
            center = Offset(px + engine.playerWidth * 0.5f, py - engine.playerHeight * 0.5f)
        )
    }

    // Draw Modi Character using Canvas primitives
    val cx = px + engine.playerWidth * 0.5f
    val h = engine.playerHeight
    val topY = py - (if (engine.isSliding) h * 0.5f else h)
    val scale = h / 100f

    // Legs / Lower body
    if (engine.isSliding) {
        // Sliding posture
        drawRoundRect(
            color = Color(0xFFF5F5F5),
            topLeft = Offset(cx - 24f, py - 18f),
            size = Size(48f, 18f),
            cornerRadius = CornerRadius(6f)
        )
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(cx + 20f, py - 14f),
            size = Size(16f, 12f),
            cornerRadius = CornerRadius(4f)
        )
    } else {
        val legSwing = if (pose == ModiPose.Jumping) 0f else sin(engine.animTime * (if (engine.isTurboActive) 22f else 14f)) * 14f
        // Left leg
        drawRoundRect(
            color = Color(0xFFF5F5F5),
            topLeft = Offset(cx - 14f - legSwing * 0.3f, py - 32f),
            size = Size(10f, 32f),
            cornerRadius = CornerRadius(4f)
        )
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(cx - 16f - legSwing * 0.3f, py - 8f),
            size = Size(14f, 8f),
            cornerRadius = CornerRadius(3f)
        )
        // Right leg
        drawRoundRect(
            color = Color(0xFFF5F5F5),
            topLeft = Offset(cx + 4f + legSwing * 0.3f, py - 32f),
            size = Size(10f, 32f),
            cornerRadius = CornerRadius(4f)
        )
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(cx + 3f + legSwing * 0.3f, py - 8f),
            size = Size(14f, 8f),
            cornerRadius = CornerRadius(3f)
        )
    }

    // Kurta & Modi Vest (Jacket)
    val bodyTop = topY + (if (engine.isSliding) 6f else 32f)
    val bodyHeight = if (engine.isSliding) 24f else 38f

    // White Kurta
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - 18f, bodyTop),
        size = Size(36f, bodyHeight),
        cornerRadius = CornerRadius(4f)
    )

    // Saffron Nehru Vest
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF8F00), Color(0xFFE65100)),
            startY = bodyTop,
            endY = bodyTop + bodyHeight
        ),
        topLeft = Offset(cx - 16f, bodyTop),
        size = Size(32f, bodyHeight),
        cornerRadius = CornerRadius(4f)
    )

    // Tricolor pocket emblem
    drawRect(color = Color(0xFFFF9933), topLeft = Offset(cx - 12f, bodyTop + 6f), size = Size(6f, 2f))
    drawRect(color = Color(0xFFFFFFFF), topLeft = Offset(cx - 12f, bodyTop + 8f), size = Size(6f, 2f))
    drawRect(color = Color(0xFF138808), topLeft = Offset(cx - 12f, bodyTop + 10f), size = Size(6f, 2f))

    // Fluttering Saffron Scarf
    val wave = sin(engine.animTime * 14f) * 6f
    drawLine(
        color = Color(0xFFFF5722),
        start = Offset(cx - 10f, bodyTop + 2f),
        end = Offset(cx - 24f + wave, bodyTop + 22f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Head, Hair, Beard, Specs
    val headY = bodyTop - 24f
    // Face
    drawOval(color = Color(0xFFFFE0B2), topLeft = Offset(cx - 13f, headY), size = Size(26f, 24f))
    // White Hair
    drawArc(
        color = Color(0xFFF5F5F5),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - 14f, headY - 4f),
        size = Size(28f, 20f)
    )
    // White Beard
    drawArc(
        color = Color(0xFFF0F0F0),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(cx - 12f, headY + 8f),
        size = Size(24f, 18f)
    )

    // Glasses
    val glassesColor = if (engine.isTurboActive) Color(0xFFFFD54F) else Color(0xFF212121)
    drawOval(
        color = glassesColor,
        topLeft = Offset(cx - 10f, headY + 6f),
        size = Size(8f, 7f),
        style = Stroke(width = 2f)
    )
    drawOval(
        color = glassesColor,
        topLeft = Offset(cx + 2f, headY + 6f),
        size = Size(8f, 7f),
        style = Stroke(width = 2f)
    )
    drawLine(
        color = glassesColor,
        start = Offset(cx - 2f, headY + 9f),
        end = Offset(cx + 2f, headY + 9f),
        strokeWidth = 2f
    )
    // Eyes
    drawCircle(color = Color(0xFF212121), radius = 1.2f, center = Offset(cx - 6f, headY + 9.5f))
    drawCircle(color = Color(0xFF212121), radius = 1.2f, center = Offset(cx + 6f, headY + 9.5f))

    // Arm holding broom if clean combo active, otherwise running arm swing
    if (engine.isMultiplierActive) {
        // Holding clean broom forward
        drawLine(
            color = Color.White,
            start = Offset(cx + 12f, bodyTop + 10f),
            end = Offset(cx + 26f, bodyTop + 4f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        // Mini broom
        drawLine(
            color = Color(0xFF8D6E63),
            start = Offset(cx + 26f, bodyTop + 4f),
            end = Offset(cx + 42f, bodyTop - 6f),
            strokeWidth = 3f
        )
        drawCircle(color = Color(0xFFFDD835), radius = 6f, center = Offset(cx + 42f, bodyTop - 6f))
    }
}

private fun DrawScope.drawSpeechBanners(banners: List<com.example.game.FloatingSpeech>) {
    for (banner in banners) {
        val y = banner.y
        val x = banner.x
        // Glowing speech bubble background
        drawRoundRect(
            color = Color(0xFF0D253F).copy(alpha = banner.alpha * 0.88f),
            topLeft = Offset(x - 130f, y - 20f),
            size = Size(260f, 38f),
            cornerRadius = CornerRadius(14f)
        )
        drawRoundRect(
            color = Color(0xFFFFD54F).copy(alpha = banner.alpha),
            topLeft = Offset(x - 130f, y - 20f),
            size = Size(260f, 38f),
            cornerRadius = CornerRadius(14f),
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawSpeedLines(w: Float, h: Float, animTime: Float) {
    for (i in 0..10) {
        val y = (i * 180f + (animTime * 1200f)) % h
        val len = 80f + ((i * 37) % 120)
        val x = (w * 0.9f) - ((animTime * 1800f + i * 150f) % (w * 1.2f))
        drawLine(
            color = Color(0x66FFD54F),
            start = Offset(x, y),
            end = Offset(x + len, y),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}
