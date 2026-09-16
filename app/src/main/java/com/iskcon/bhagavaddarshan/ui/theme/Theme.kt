package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Marigold,
    onPrimary = Color.White,
    primaryContainer = MarigoldContainer,
    onPrimaryContainer = MarigoldDeep,
    inversePrimary = MarigoldLight,
    secondary = SacredGold,
    onSecondary = Color.White,
    secondaryContainer = SacredGoldLight,
    onSecondaryContainer = MarigoldDeep,
    tertiary = TempleMaroon,
    onTertiary = Color.White,
    tertiaryContainer = TempleMaroonLight,
    onTertiaryContainer = TempleMaroonDeep,
    background = Color(0xFFF6EDE3),
    onBackground = UxInk,
    surface = Color(0xFFFFFBF6),
    onSurface = Ink,
    surfaceVariant = Color(0xFFF5EDE3),
    onSurfaceVariant = InkSoft,
    surfaceTint = Marigold,
    outline = Outline,
    outlineVariant = Color(0xFFE8DDD0),
    error = SoftRed,
    onError = Color.White,
    errorContainer = SoftRedContainer,
    onErrorContainer = Color(0xFF5C0F0F)
)

@Composable
fun BhagavadDarshanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
