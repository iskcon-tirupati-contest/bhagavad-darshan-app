package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    primaryContainer = CreamDark,
    onPrimaryContainer = Ink,
    secondary = Leaf,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    error = SoftRed
)

private val DarkColors = darkColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    background = Color(0xFF1A1208),
    onBackground = Cream,
    surface = Color(0xFF2A1F14),
    onSurface = Cream
)

@Composable
fun BhagavadDarshanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
