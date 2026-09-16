package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.ui.theme.IvoryDeep
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.SacredGold
import com.iskcon.bhagavaddarshan.ui.theme.TempleMaroon
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold100

/** Soft multi-stop wash behind screens — adds depth without looking flat. */
val ScreenAtmosphereBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFBF6),
        UxCream,
        Color(0xFFF6EDE3),
        Color(0xFFF3E8DC)
    )
)

val PremiumCardBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFE), Color(0xFFFFF8F0))
)

val ChromeTopBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFCF8), IvoryDeep, Color(0xFFF7EFE6))
)

val ChromeBottomBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFCF8), Color(0xFFF8F0E6))
)

fun Modifier.premiumShadow(
    shape: Shape,
    elevation: Dp = 10.dp,
    tint: Color = TempleMaroon
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        spotColor = tint.copy(alpha = 0.18f),
        ambientColor = Color(0xFF3E2723).copy(alpha = 0.10f)
    )

fun Modifier.premiumCardSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 10.dp,
    brush: Brush = PremiumCardBrush,
    borderColor: Color = Color(0x33C9A227)
): Modifier = this
    .premiumShadow(shape, elevation)
    .clip(shape)
    .background(brush)
    .border(1.dp, borderColor, shape)

@Composable
fun PremiumScreenBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenAtmosphereBrush),
        content = content
    )
}

@Composable
fun PremiumPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .premiumCardSurface(shape = shape),
        content = content
    )
}

/** Thin gold sheen line used under headers / above bottom nav. */
@Composable
fun PremiumGoldHairline(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        SacredGold.copy(alpha = 0.55f),
                        Marigold.copy(alpha = 0.45f),
                        SacredGold.copy(alpha = 0.55f),
                        Color.Transparent
                    )
                )
            )
    )
}
