package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = SaffronDark,
    onPrimaryContainer = Color.White,
    secondary = GoldYellow,
    onSecondary = Color.Black,
    secondaryContainer = NavySurface,
    onSecondaryContainer = Color.White,
    tertiary = IndiaGreenLight,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFF0F0F0),
    surface = DarkSurface,
    onSurface = Color(0xFFF0F0F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD0D8E0),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = Color(0xFF4A1E00),
    secondary = NavyBlue,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Color(0xFF3E1C00),
    tertiary = IndiaGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1F1D1B),
    surface = LightSurface,
    onSurface = Color(0xFF1F1D1B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5C4738),
  )

@Composable
fun ModiGameTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep patriotic branded colors consistent
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
