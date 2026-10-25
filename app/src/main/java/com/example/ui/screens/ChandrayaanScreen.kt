package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChandrayaanEngine
import com.example.ui.GameViewModel
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun ChandrayaanScreen(viewModel: GameViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    var missionResult by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }

    val engine = remember {
        ChandrayaanEngine(
            soundManager = viewModel.soundManager,
            hapticManager = viewModel.hapticManager,
            onMissionComplete = { score, success ->
                missionResult = Pair(score, success)
                viewModel.recordChandrayaanResult(score, success)
            }
        )
    }

    var initialized by remember { mutableStateOf(false) }
    var currentTick by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060B12))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        LaunchedEffect(widthPx, heightPx) {
            if (widthPx > 0f && heightPx > 0f && !initialized) {
                engine.reset(widthPx, heightPx)
                initialized = true
            }
        }

        // Real-time Physics Engine loop
        LaunchedEffect(initialized) {
            if (!initialized) return@LaunchedEffect
            var lastTime = 0L
            while (true) {
                withFrameNanos { timeNanos ->
                    if (lastTime != 0L) {
                        val dt = ((timeNanos - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                        engine.update(dt)
                        currentTick = timeNanos.toFloat()
                    }
                    lastTime = timeNanos
                }
            }
        }

        // Canvas Lunar Simulation View
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val frame = currentTick

            drawSpaceAndStars(size.width, size.height)
            drawEarth(size.width)
            drawMoonSurface(size.width, size.height, engine.padY, engine.padX, engine.padWidth)
            drawVikramLander(engine)

            if (engine.status == ChandrayaanEngine.LandingStatus.LandedSuccess) {
                drawPragyanRoverAndFlag(
                    engine.padX,
                    engine.padY,
                    engine.roverDeployProgress
                )
            }
        }

        // Top Navigation & Telemetry HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("back_from_chandrayaan")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC0D253F)
                ) {
                    Text(
                        text = "🚀 CHANDRAYAAN 3 • MOON SOUTH POLE",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Restart button
                IconButton(
                    onClick = {
                        missionResult = null
                        engine.reset(widthPx, heightPx)
                    },
                    modifier = Modifier.testTag("restart_chandrayaan_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Restart",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Telemetry Grid
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xDD0D1B2A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4400E5FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Altitude
                        val altitude = ((engine.padY - engine.posY).toInt()).coerceAtLeast(0)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ALTITUDE", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text("${altitude}m", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Descent Speed (Safe threshold: 105)
                        val safeVelY = engine.velY <= 105f
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DESCENT SPEED", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${engine.velY.toInt()} m/s",
                                color = if (safeVelY) Color(0xFF81C784) else Color(0xFFFF5252),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Horizontal Speed
                        val safeVelX = abs(engine.velX) <= 55f
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("H-SPEED", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${abs(engine.velX).toInt()} m/s",
                                color = if (safeVelX) Color(0xFF81C784) else Color(0xFFFF5252),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fuel Gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FUEL:", color = Color(0xFFB0BEC5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { engine.fuel / 100f },
                            color = if (engine.fuel > 25f) Color(0xFF00E5FF) else Color(0xFFFF5252),
                            trackColor = Color(0xFF263238),
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${engine.fuel.toInt()}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // On-Screen Thruster Controls (Bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Thruster (Rotates Right)
            ThrusterButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "LEFT RCS",
                isActive = engine.isLeftThrusterActive,
                onStateChange = { engine.isLeftThrusterActive = it },
                tag = "left_thruster_btn"
            )

            // Main Retro Thruster (Big center button)
            Surface(
                shape = CircleShape,
                color = if (engine.isMainThrusterActive) Color(0xFFFF6F00) else Color(0xCC0D253F),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFFFD54F)),
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                engine.isMainThrusterActive = true
                                tryAwaitRelease()
                                engine.isMainThrusterActive = false
                            }
                        )
                    }
                    .testTag("main_thruster_btn")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Main Thruster",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "MAIN THRUST",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Right Thruster (Rotates Left)
            ThrusterButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                label = "RIGHT RCS",
                isActive = engine.isRightThrusterActive,
                onStateChange = { engine.isRightThrusterActive = it },
                tag = "right_thruster_btn"
            )
        }

        // Mission Outcome Dialog
        AnimatedVisibility(
            visible = missionResult != null,
            enter = fadeIn() + scaleIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            missionResult?.let { (score, success) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2B)),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = if (success) Color(0xFFFFD54F) else Color(0xFFFF5252)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (success) "🇮🇳 SHIV SHAKTI POINT TOUCHDOWN! 🇮🇳" else "💥 HARD CRASH LANDING",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (success) Color(0xFFFFD54F) else Color(0xFFFF5252),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (success)
                                "Vikram Lander touched down softly! Pragyan Rover deployed and Indian Tricolor planted on the Moon South Pole!"
                            else
                                "Descent speed was too high! Tap Retry to master the lunar thruster controls.",
                            fontSize = 13.sp,
                            color = Color(0xFFCFD8DC),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (success) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x3300E5FF),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Reward: ", color = Color.White, fontSize = 13.sp)
                                    Text("🪙 +${50 + score / 10} Coins  •  ⚡ +500 VP", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                missionResult = null
                                engine.reset(widthPx, heightPx)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (success) Color(0xFFFF8F00) else Color(0xFFD32F2F)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("chandrayaan_retry_btn")
                        ) {
                            Text(if (success) "LAND AGAIN 🚀" else "RETRY LANDING", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                missionResult = null
                                viewModel.navigateBack()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("chandrayaan_menu_btn")
                        ) {
                            Text("Main Menu")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThrusterButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onStateChange: (Boolean) -> Unit,
    tag: String
) {
    Surface(
        shape = CircleShape,
        color = if (isActive) Color(0xFF00E5FF) else Color(0xCC0D253F),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onStateChange(true)
                        tryAwaitRelease()
                        onStateChange(false)
                    }
                )
            }
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = if (isActive) Color.Black else Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Canvas Drawings for Chandrayaan

private fun DrawScope.drawSpaceAndStars(w: Float, h: Float) {
    // Deep starry void
    for (i in 0..40) {
        val sx = (i * 137f) % w
        val sy = (i * 219f) % (h * 0.7f)
        val r = if (i % 5 == 0) 2.2f else 1.2f
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = r, center = Offset(sx, sy))
    }
}

private fun DrawScope.drawEarth(w: Float) {
    // Distant blue marble Earth
    val ex = w * 0.85f
    val ey = 140f
    drawCircle(color = Color(0xFF1E88E5), radius = 34f, center = Offset(ex, ey))
    drawCircle(color = Color(0xFF81C784), radius = 18f, center = Offset(ex - 8f, ey - 4f))
    drawCircle(color = Color(0x33FFFFFF), radius = 38f, center = Offset(ex, ey))
}

private fun DrawScope.drawMoonSurface(w: Float, h: Float, padY: Float, padX: Float, padWidth: Float) {
    // Moon terrain base
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF455A64), Color(0xFF263238), Color(0xFF1C252B)),
            startY = padY,
            endY = h
        ),
        topLeft = Offset(0f, padY),
        size = Size(w, h - padY)
    )

    // Craters on left and right
    drawOval(color = Color(0xFF37474F), topLeft = Offset(30f, padY + 10f), size = Size(100f, 32f))
    drawOval(color = Color(0xFF37474F), topLeft = Offset(w - 140f, padY + 14f), size = Size(110f, 36f))

    // Marked Landing Pad: "SHIV SHAKTI POINT"
    val padLeft = padX - padWidth * 0.5f
    drawRoundRect(
        color = Color(0xFF0D253F),
        topLeft = Offset(padLeft, padY - 4f),
        size = Size(padWidth, 12f),
        cornerRadius = CornerRadius(4f)
    )
    drawRoundRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(padLeft, padY - 4f),
        size = Size(padWidth, 12f),
        cornerRadius = CornerRadius(4f),
        style = Stroke(width = 2f)
    )

    // Glowing beacons on pad corners
    drawCircle(color = Color(0xFF00E5FF), radius = 5f, center = Offset(padLeft + 8f, padY + 2f))
    drawCircle(color = Color(0xFF00E5FF), radius = 5f, center = Offset(padLeft + padWidth - 8f, padY + 2f))
}

private fun DrawScope.drawVikramLander(engine: ChandrayaanEngine) {
    val x = engine.posX
    val y = engine.posY

    rotate(engine.angleDeg, pivot = Offset(x, y)) {
        // Main Thruster Flame
        if (engine.isMainThrusterActive && engine.fuel > 0f) {
            val flameHeight = 44f + (Math.random() * 14f).toFloat()
            val flamePath = Path().apply {
                moveTo(x - 12f, y + 26f)
                lineTo(x, y + 26f + flameHeight)
                lineTo(x + 12f, y + 26f)
                close()
            }
            drawPath(
                flamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF176), Color(0xFFFF9800), Color(0xFFFF3D00)),
                    startY = y + 26f,
                    endY = y + 26f + flameHeight
                )
            )
        }

        // Lander Landing Legs (4 legs with shock absorber pads)
        drawLine(color = Color(0xFFCFD8DC), start = Offset(x - 18f, y + 16f), end = Offset(x - 36f, y + 36f), strokeWidth = 3f)
        drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(x - 36f, y + 36f))

        drawLine(color = Color(0xFFCFD8DC), start = Offset(x + 18f, y + 16f), end = Offset(x + 36f, y + 36f), strokeWidth = 3f)
        drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(x + 36f, y + 36f))

        // Gold multi-layer insulation body (MLI)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF8F00)),
                startY = y - 18f,
                endY = y + 18f
            ),
            topLeft = Offset(x - 24f, y - 18f),
            size = Size(48f, 36f),
            cornerRadius = CornerRadius(6f)
        )

        // Solar Panels (Top wings)
        drawRect(color = Color(0xFF1565C0), topLeft = Offset(x - 38f, y - 12f), size = Size(14f, 22f))
        drawRect(color = Color(0xFF1565C0), topLeft = Offset(x + 24f, y - 12f), size = Size(14f, 22f))

        // ISRO Indian Tricolor emblem on body
        drawRect(color = Color(0xFFFF9933), topLeft = Offset(x - 10f, y - 6f), size = Size(20f, 3f))
        drawRect(color = Color(0xFFFFFFFF), topLeft = Offset(x - 10f, y - 3f), size = Size(20f, 3f))
        drawRect(color = Color(0xFF138808), topLeft = Offset(x - 10f, y), size = Size(20f, 3f))

        // Top communication antenna dish
        drawLine(color = Color.White, start = Offset(x, y - 18f), end = Offset(x, y - 28f), strokeWidth = 2f)
        drawCircle(color = Color(0xFFECEFF1), radius = 4f, center = Offset(x, y - 28f))
    }
}

private fun DrawScope.drawPragyanRoverAndFlag(padX: Float, padY: Float, progress: Float) {
    // Ramp extension
    val rampEndX = padX + 35f * progress
    drawLine(
        color = Color(0xFF90A4AE),
        start = Offset(padX + 16f, padY - 6f),
        end = Offset(rampEndX, padY),
        strokeWidth = 3f
    )

    // Pragyan Rover body
    if (progress > 0.4f) {
        val roverX = padX + 50f * (progress - 0.4f) * 1.6f
        val roverY = padY - 12f

        // Rover Chassis
        drawRoundRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(roverX - 12f, roverY - 8f),
            size = Size(24f, 12f),
            cornerRadius = CornerRadius(3f)
        )
        // 6 Rover wheels
        for (w in 0..2) {
            drawCircle(color = Color(0xFF263238), radius = 3.5f, center = Offset(roverX - 8f + w * 8f, roverY + 6f))
        }

        // Indian Tricolor Flag planted on Moon soil!
        if (progress > 0.8f) {
            val flagX = roverX + 26f
            val flagY = padY - 26f
            // Flagpole
            drawLine(
                color = Color.White,
                start = Offset(flagX, padY),
                end = Offset(flagX, flagY),
                strokeWidth = 2f
            )
            // Saffron, White, Green Flag
            drawRect(color = Color(0xFFFF9933), topLeft = Offset(flagX, flagY), size = Size(18f, 5f))
            drawRect(color = Color(0xFFFFFFFF), topLeft = Offset(flagX, flagY + 5f), size = Size(18f, 5f))
            drawRect(color = Color(0xFF138808), topLeft = Offset(flagX, flagY + 10f), size = Size(18f, 5f))
            // Blue chakra dot
            drawCircle(color = Color(0xFF000080), radius = 1.8f, center = Offset(flagX + 9f, flagY + 7.5f))
        }
    }
}
