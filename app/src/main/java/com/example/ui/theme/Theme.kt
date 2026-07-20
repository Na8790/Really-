package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MochaGold,
    secondary = DesertSand,
    tertiary = EmeraldYemen,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface,
    primaryContainer = MudBrickAmber,
    onPrimaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = EmeraldYemen,
    background = LightBackground,
    surface = LightSurface,
    onBackground = OnLightSurface,
    onSurface = OnLightSurface,
    primaryContainer = MochaGold,
    onPrimaryContainer = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
