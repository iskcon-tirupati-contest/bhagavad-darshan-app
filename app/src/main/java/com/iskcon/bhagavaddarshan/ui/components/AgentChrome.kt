package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.R
import com.iskcon.bhagavaddarshan.ui.theme.ChromeBar
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.MotionSpecs
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
import com.iskcon.bhagavaddarshan.ui.theme.UxSoftShadow
import com.iskcon.bhagavaddarshan.util.Haptics

/** Soft cream nav bar (customer-style), selected = temple green. */
val AgentNavBarGreen = ChromeBar
val AgentNavBarSelected = TempleGreen

@Composable
fun AgentHeaderTitle(
    title: String,
    subtitle: String? = null,
    /** When true with a subtitle, show "title, subtitle" on one line (e.g. Hare Krishna, Name). */
    singleLine: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.bd_logo),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(10.dp))
        if (singleLine && !subtitle.isNullOrBlank()) {
            Text(
                "$title, $subtitle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TempleGreen,
                fontSize = 16.sp,
                maxLines = 1
            )
        } else {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TempleGreen,
                    fontSize = 17.sp,
                    maxLines = 1
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = EditMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ShadowCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    elevation: Dp = 6.dp,
    containerColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    LaunchedEffect(pressed) {
        if (pressed && onClick != null) Haptics.tap(view)
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "shadowCardScale"
    )
    val elev by animateDpAsState(
        targetValue = if (pressed && onClick != null) 2.dp else elevation,
        label = "shadowCardElev"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elev, shape, spotColor = UxSoftShadow, ambientColor = Color(0x22000000))
            .clip(shape)
            .background(containerColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

@Composable
fun PressablePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    containerColor: Color = Marigold,
    pressedColor: Color = Color(0xFFC48A14)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    LaunchedEffect(pressed) {
        if (pressed && enabled) Haptics.tap(view)
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.94f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "primaryBtnScale"
    )
    val elev by animateDpAsState(
        targetValue = if (pressed && enabled) 1.dp else if (compact) 4.dp else 10.dp,
        label = "primaryBtnElev"
    )
    val shape = RoundedCornerShape(if (compact) 12.dp else 16.dp)
    val fill = if (pressed && enabled) pressedColor else containerColor
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (pressed && enabled) 3f else 0f
                alpha = if (enabled) 1f else 0.45f
            }
            .shadow(elev, shape, spotColor = fill.copy(alpha = 0.45f), ambientColor = fill.copy(alpha = 0.22f))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(fill.copy(alpha = 0.95f), fill)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .defaultMinSize(minHeight = if (compact) 40.dp else 52.dp)
            .padding(
                vertical = if (compact) 8.dp else 14.dp,
                horizontal = if (compact) 14.dp else 18.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 14.sp else 16.sp
        )
    }
}

@Composable
fun PressableOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    borderColor: Color = Marigold,
    textColor: Color = Marigold
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    LaunchedEffect(pressed) {
        if (pressed) Haptics.tap(view)
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "outlineBtnScale"
    )
    val shape = RoundedCornerShape(if (compact) 12.dp else 14.dp)
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (pressed) 1.dp else if (compact) 2.dp else 4.dp,
                shape = shape,
                spotColor = borderColor.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(if (pressed) borderColor.copy(alpha = 0.12f) else Color.White)
            .border(BorderStroke(if (compact) 1.2.dp else 1.6.dp, borderColor), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = if (compact) 40.dp else 48.dp)
            .padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = if (compact) 8.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (compact) 14.sp else 15.sp
        )
    }
}

@Composable
fun PressableDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    PressablePrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        compact = compact,
        containerColor = Color(0xFFC62828),
        pressedColor = Color(0xFF8E0000)
    )
}

/** Compact trash icon with soft press — used on devotee rows. */
@Composable
fun PressableDeleteIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    LaunchedEffect(pressed) {
        if (pressed) Haptics.firm(view)
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "deleteIconScale"
    )
    val elev by animateDpAsState(
        targetValue = if (pressed) 1.dp else 6.dp,
        label = "deleteIconElev"
    )
    val fill = if (pressed) Color(0xFF8E0000) else Color(0xFFC62828)
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (pressed) 2f else 0f
            }
            .shadow(elev, CircleShape, spotColor = fill.copy(alpha = 0.4f))
            .size(40.dp)
            .clip(CircleShape)
            .background(fill)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Emerged circular + button with soft press depth. */
@Composable
fun PressableFab(
    onClick: () -> Unit,
    containerColor: Color = Marigold,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current
    LaunchedEffect(pressed) {
        if (pressed) Haptics.tap(view)
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "fabScale"
    )
    val elev by animateDpAsState(
        targetValue = if (pressed) 2.dp else 14.dp,
        label = "fabElev"
    )
    val fill = if (pressed) containerColor.copy(alpha = 0.88f) else containerColor
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (pressed) 4f else 0f
            }
            .shadow(
                elevation = elev,
                shape = CircleShape,
                spotColor = fill.copy(alpha = 0.5f),
                ambientColor = fill.copy(alpha = 0.25f)
            )
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(fill.copy(alpha = 0.92f), fill)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
