package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

enum class ModiPose {
    Standing,
    Namaste,
    Running,
    Jumping,
    Sliding,
    Turbo
}

@Composable
fun ModiAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    pose: ModiPose = ModiPose.Standing,
    animTime: Float = 0f,
    hasShield: Boolean = false
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Shield bubble glow if active
            if (hasShield) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3300E5FF),
                            Color(0x66FFD700),
                            Color(0x99FF9933)
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.52f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                drawCircle(
                    color = Color(0xFFFFD54F),
                    style = Stroke(width = 3.dp.toPx()),
                    radius = w * 0.52f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }

            // Turbo speed aura
            if (pose == ModiPose.Turbo) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x88FF9933), Color(0x33FF6F00), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.6f
                    ),
                    radius = w * 0.6f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }

            drawModiCharacter(w, h, pose, animTime)
        }
    }
}

private fun DrawScope.drawModiCharacter(w: Float, h: Float, pose: ModiPose, animTime: Float) {
    val scale = w / 100f
    val cx = w * 0.5f

    // Legs animation
    val legSwing = if (pose == ModiPose.Running || pose == ModiPose.Turbo) {
        val freq = if (pose == ModiPose.Turbo) 18f else 12f
        sin(animTime * freq) * 16f * scale
    } else 0f

    val legY = h * 0.68f
    val legHeight = h * 0.28f

    // Draw Legs / Pajama pants
    when (pose) {
        ModiPose.Sliding -> {
            // Legs stretched forward
            drawRoundRect(
                color = Color(0xFFF0F0F0),
                topLeft = Offset(cx - 20 * scale, legY + 8 * scale),
                size = Size(36 * scale, 12 * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6 * scale)
            )
            // Black formal shoes
            drawRoundRect(
                color = Color(0xFF1E1E1E),
                topLeft = Offset(cx + 14 * scale, legY + 10 * scale),
                size = Size(12 * scale, 8 * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
        }
        ModiPose.Jumping -> {
            // Tucked legs
            drawRoundRect(
                color = Color(0xFFF5F5F5),
                topLeft = Offset(cx - 14 * scale, legY),
                size = Size(10 * scale, legHeight * 0.7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
            drawRoundRect(
                color = Color(0xFFF5F5F5),
                topLeft = Offset(cx + 4 * scale, legY - 4 * scale),
                size = Size(10 * scale, legHeight * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
            // Shoes
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(cx - 15 * scale, legY + legHeight * 0.65f),
                size = Size(13 * scale, 7 * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3 * scale)
            )
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(cx + 4 * scale, legY + legHeight * 0.55f),
                size = Size(13 * scale, 7 * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3 * scale)
            )
        }
        else -> {
            // Left Leg
            val leftLegOffset = Offset(cx - 13 * scale - legSwing * 0.4f, legY)
            drawRoundRect(
                color = Color(0xFFF5F5F5),
                topLeft = leftLegOffset,
                size = Size(9 * scale, legHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
            // Right Leg
            val rightLegOffset = Offset(cx + 4 * scale + legSwing * 0.4f, legY)
            drawRoundRect(
                color = Color(0xFFF5F5F5),
                topLeft = rightLegOffset,
                size = Size(9 * scale, legHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
            // Formal shoes (Dark brown / black loafers)
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(leftLegOffset.x - 2 * scale, legY + legHeight - 2 * scale),
                size = Size(14 * scale, 8 * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(rightLegOffset.x - 1 * scale, legY + legHeight - 2 * scale),
                size = Size(14 * scale, 8 * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4 * scale)
            )
        }
    }

    // Billowing White Kurta
    val kurtaPath = Path().apply {
        moveTo(cx - 18 * scale, h * 0.45f)
        lineTo(cx + 18 * scale, h * 0.45f)
        lineTo(cx + 21 * scale, h * 0.72f)
        lineTo(cx - 21 * scale, h * 0.72f)
        close()
    }
    drawPath(kurtaPath, color = Color(0xFFFFFFFF))

    // Iconic Modi Jacket (Saffron/Gold Nehru vest with mandarin collar)
    val jacketPath = Path().apply {
        moveTo(cx - 16 * scale, h * 0.45f)
        lineTo(cx + 16 * scale, h * 0.45f)
        lineTo(cx + 18 * scale, h * 0.69f)
        lineTo(cx - 18 * scale, h * 0.69f)
        close()
    }
    drawPath(
        jacketPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF8F00), Color(0xFFE65100))
        )
    )

    // Jacket button seam and buttons
    drawLine(
        color = Color(0xFFBF360C),
        start = Offset(cx, h * 0.45f),
        end = Offset(cx, h * 0.68f),
        strokeWidth = 2 * scale
    )
    for (i in 0..3) {
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 1.6f * scale,
            center = Offset(cx, h * 0.49f + i * 5f * scale)
        )
    }

    // Tiranga Pocket Square / Badge on vest
    drawRect(
        color = Color(0xFFFF9933),
        topLeft = Offset(cx - 13 * scale, h * 0.52f),
        size = Size(6 * scale, 2 * scale)
    )
    drawRect(
        color = Color(0xFFFFFFFF),
        topLeft = Offset(cx - 13 * scale, h * 0.54f),
        size = Size(6 * scale, 2 * scale)
    )
    drawRect(
        color = Color(0xFF138808),
        topLeft = Offset(cx - 13 * scale, h * 0.56f),
        size = Size(6 * scale, 2 * scale)
    )

    // Fluttering Saffron Scarf (Gamcha / Shawl)
    val scarfWave = sin(animTime * 10f) * 6f * scale
    val scarfPath = Path().apply {
        moveTo(cx - 14 * scale, h * 0.44f)
        quadraticTo(
            cx - 22 * scale + scarfWave,
            h * 0.52f,
            cx - 24 * scale + scarfWave * 1.5f,
            h * 0.66f
        )
        lineTo(cx - 20 * scale + scarfWave * 1.5f, h * 0.66f)
        quadraticTo(
            cx - 18 * scale + scarfWave,
            h * 0.52f,
            cx - 10 * scale,
            h * 0.44f
        )
        close()
    }
    drawPath(scarfPath, color = Color(0xFFFF7043))

    // Head Base (Neck + Face)
    // Mandarin Collar
    drawRoundRect(
        color = Color(0xFFD84315),
        topLeft = Offset(cx - 8 * scale, h * 0.41f),
        size = Size(16 * scale, 5 * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2 * scale)
    )

    // Face oval
    drawOval(
        color = Color(0xFFFFE0B2),
        topLeft = Offset(cx - 15 * scale, h * 0.18f),
        size = Size(30 * scale, 26 * scale)
    )

    // Classic White Hair contour around head
    val hairPath = Path().apply {
        moveTo(cx - 16 * scale, h * 0.28f)
        cubicTo(
            cx - 18 * scale, h * 0.14f,
            cx + 18 * scale, h * 0.14f,
            cx + 16 * scale, h * 0.28f
        )
        lineTo(cx + 14 * scale, h * 0.24f)
        cubicTo(
            cx + 12 * scale, h * 0.18f,
            cx - 12 * scale, h * 0.18f,
            cx - 14 * scale, h * 0.24f
        )
        close()
    }
    drawPath(hairPath, color = Color(0xFFF5F5F5))

    // Iconic White Beard and Moustache
    val beardPath = Path().apply {
        moveTo(cx - 14 * scale, h * 0.27f)
        cubicTo(
            cx - 14 * scale, h * 0.41f,
            cx + 14 * scale, h * 0.41f,
            cx + 14 * scale, h * 0.27f
        )
        cubicTo(
            cx + 8 * scale, h * 0.32f,
            cx - 8 * scale, h * 0.32f,
            cx - 14 * scale, h * 0.27f
        )
        close()
    }
    drawPath(beardPath, color = Color(0xFFF0F0F0))

    // Moustache
    val moustachePath = Path().apply {
        moveTo(cx - 8 * scale, h * 0.29f)
        quadraticTo(cx - 2 * scale, h * 0.32f, cx, h * 0.30f)
        quadraticTo(cx + 2 * scale, h * 0.32f, cx + 8 * scale, h * 0.29f)
        quadraticTo(cx, h * 0.33f, cx - 8 * scale, h * 0.29f)
        close()
    }
    drawPath(moustachePath, color = Color(0xFFE0E0E0))

    // Friendly smile inside beard
    drawArc(
        color = Color(0xFFD84315),
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(cx - 4 * scale, h * 0.32f),
        size = Size(8 * scale, 4 * scale),
        style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round)
    )

    // Distinctive Rimless Spectacles / Glasses
    val glassesFrameColor = if (pose == ModiPose.Turbo) Color(0xFFFFD54F) else Color(0xFF263238)
    val glassesStroke = 2.2f * scale

    // Left lens
    drawOval(
        color = glassesFrameColor,
        topLeft = Offset(cx - 12 * scale, h * 0.22f),
        size = Size(9 * scale, 7.5f * scale),
        style = Stroke(width = glassesStroke)
    )
    // Right lens
    drawOval(
        color = glassesFrameColor,
        topLeft = Offset(cx + 3 * scale, h * 0.22f),
        size = Size(9 * scale, 7.5f * scale),
        style = Stroke(width = glassesStroke)
    )
    // Bridge between lenses
    drawLine(
        color = glassesFrameColor,
        start = Offset(cx - 3 * scale, h * 0.25f),
        end = Offset(cx + 3 * scale, h * 0.25f),
        strokeWidth = glassesStroke
    )

    // Lens subtle reflection
    drawLine(
        color = Color(0x99FFFFFF),
        start = Offset(cx - 10 * scale, h * 0.23f),
        end = Offset(cx - 6 * scale, h * 0.27f),
        strokeWidth = 1.5f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0x99FFFFFF),
        start = Offset(cx + 5 * scale, h * 0.23f),
        end = Offset(cx + 9 * scale, h * 0.27f),
        strokeWidth = 1.5f * scale,
        cap = StrokeCap.Round
    )

    // Eyes
    drawCircle(
        color = Color(0xFF212121),
        radius = 1.5f * scale,
        center = Offset(cx - 7.5f * scale, h * 0.255f)
    )
    drawCircle(
        color = Color(0xFF212121),
        radius = 1.5f * scale,
        center = Offset(cx + 7.5f * scale, h * 0.255f)
    )

    // Arms based on pose
    when (pose) {
        ModiPose.Namaste -> {
            // Folded hands in front of chest (Namaste greeting)
            val armPath = Path().apply {
                moveTo(cx - 16 * scale, h * 0.47f)
                lineTo(cx - 2 * scale, h * 0.52f)
                lineTo(cx, h * 0.48f)
                lineTo(cx + 2 * scale, h * 0.52f)
                lineTo(cx + 16 * scale, h * 0.47f)
            }
            drawPath(armPath, color = Color(0xFFFFFFFF), style = Stroke(width = 6 * scale, cap = StrokeCap.Round))
            // Hands
            drawCircle(color = Color(0xFFFFE0B2), radius = 4 * scale, center = Offset(cx, h * 0.50f))
        }
        ModiPose.Running, ModiPose.Turbo -> {
            // Swinging arms
            val armSwing = sin(animTime * (if (pose == ModiPose.Turbo) 18f else 12f)) * 14f * scale
            // Left arm
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(cx - 14 * scale, h * 0.48f),
                end = Offset(cx - 18 * scale - armSwing, h * 0.58f),
                strokeWidth = 5 * scale,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFFFE0B2),
                radius = 3.5f * scale,
                center = Offset(cx - 18 * scale - armSwing, h * 0.58f)
            )
            // Right arm (often holding a Swachh broom or waving forward)
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(cx + 14 * scale, h * 0.48f),
                end = Offset(cx + 18 * scale + armSwing, h * 0.58f),
                strokeWidth = 5 * scale,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFFFE0B2),
                radius = 3.5f * scale,
                center = Offset(cx + 18 * scale + armSwing, h * 0.58f)
            )
        }
        ModiPose.Jumping -> {
            // Victorious raised arms
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(cx - 14 * scale, h * 0.48f),
                end = Offset(cx - 24 * scale, h * 0.34f),
                strokeWidth = 5 * scale,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFFFFE0B2), radius = 3.5f * scale, center = Offset(cx - 24 * scale, h * 0.34f))
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(cx + 14 * scale, h * 0.48f),
                end = Offset(cx + 24 * scale, h * 0.34f),
                strokeWidth = 5 * scale,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFFFFE0B2), radius = 3.5f * scale, center = Offset(cx + 24 * scale, h * 0.34f))
        }
        else -> {
            // Welcoming wave with one hand, other resting
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(cx - 14 * scale, h * 0.48f),
                end = Offset(cx - 22 * scale, h * 0.36f + sin(animTime * 4f) * 4 * scale),
                strokeWidth = 5 * scale,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFFFE0B2),
                radius = 3.5f * scale,
                center = Offset(cx - 22 * scale, h * 0.36f + sin(animTime * 4f) * 4 * scale)
            )
            // Left arm resting
            drawLine(
                color = Color(0xFFFFFFFF),
                start = Offset(cx + 14 * scale, h * 0.48f),
                end = Offset(cx + 18 * scale, h * 0.62f),
                strokeWidth = 5 * scale,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFFFFE0B2), radius = 3.5f * scale, center = Offset(cx + 18 * scale, h * 0.62f))
        }
    }
}
