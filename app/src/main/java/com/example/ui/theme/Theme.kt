package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = TechBlue.copy(alpha = 0.3f),
    onPrimaryContainer = CyberCyan,
    secondary = SignalGreen,
    onSecondary = Color.Black,
    secondaryContainer = SignalGreen.copy(alpha = 0.2f),
    onSecondaryContainer = SignalGreen,
    tertiary = SignalYellow,
    error = SignalRed,
    background = CityDarkBg,
    onBackground = TextPrimary,
    surface = CityDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CityDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CityDarkOutline
)

private val LightColorScheme = darkColorScheme(
    primary = TechBlue,
    onPrimary = Color.White,
    primaryContainer = TechBlue.copy(alpha = 0.2f),
    secondary = SignalGreen,
    tertiary = SignalYellow,
    error = SignalRed,
    background = CityDarkBg,
    onBackground = TextPrimary,
    surface = CityDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CityDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CityDarkOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
