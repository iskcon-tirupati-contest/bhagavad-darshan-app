package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.ui.theme.BdShape
import com.iskcon.bhagavaddarshan.ui.theme.BdSpace
import com.iskcon.bhagavaddarshan.ui.theme.BdType
import com.iskcon.bhagavaddarshan.ui.theme.EditCard
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBg
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBorder
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.MotionSpecs
import com.iskcon.bhagavaddarshan.ui.theme.TextPrimaryDark
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxGold100
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxGold500
import com.iskcon.bhagavaddarshan.ui.theme.UxGold600
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffronDark
import com.iskcon.bhagavaddarshan.ui.theme.UxSoftShadow

private val ButtonShape = BdShape.Button

@Composable
fun CustomerPageHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!eyebrow.isNullOrBlank()) {
            Text(text = eyebrow, style = BdType.Eyebrow)
            Spacer(Modifier.height(8.dp))
        }
        Text(text = title, style = BdType.PageTitle)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(text = subtitle, style = BdType.PageSubtitle)
        }
    }
}

/**
 * Standard customer surface card — soft warm border, light shadow, optional selected state.
 */
@Composable
fun BdCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    highlighted: Boolean = false,
    contentPadding: Dp = BdSpace.CardPad,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1.0f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "bdCardScale"
    )
    val shape = BdShape.Card
    val borderColor = when {
        selected || highlighted -> EditPopularBorder
        else -> UxGold200
    }
    val bg = when {
        selected || highlighted -> EditPopularBg
        else -> EditCard
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (selected) 6.dp else 3.dp,
                shape = shape,
                spotColor = UxSoftShadow,
                ambientColor = UxGold200.copy(alpha = 0.35f)
            )
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .clip(shape)
            .background(bg)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(contentPadding)
    ) {
        Column(content = content)
    }
}

@Composable
fun SoftOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "outlineBtn"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(BdShape.Button)
            .border(1.4.dp, EditTerracotta.copy(alpha = 0.55f), BdShape.Button)
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = BdType.Button,
            color = EditTerracotta,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderGlow: Boolean = false,
    corner: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    BdCard(
        modifier = modifier,
        onClick = onClick,
        selected = borderGlow,
        contentPadding = 18.dp,
        content = content
    )
}

@Composable
fun EmergedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: List<Color> = listOf(UxSaffron, UxSaffronDark),
    textColor: Color = Color.White,
    verticalPadding: Dp = 14.dp,
    fontSize: TextUnit = 16.sp,
    horizontalPadding: Dp = 20.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "emergedScale"
    )
    val fill = gradient.firstOrNull() ?: UxSaffron
    val fillBrush = Brush.horizontalGradient(
        colors = if (gradient.size >= 2) gradient else listOf(fill, fill)
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = ButtonShape,
                spotColor = fill.copy(alpha = 0.35f),
                ambientColor = fill.copy(alpha = 0.18f)
            )
            .clip(ButtonShape)
            .background(fillBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = verticalPadding, horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Montserrat,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.2.sp,
            maxLines = 1
        )
    }
}

@Composable
fun RadiantGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    EmergedButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
fun SoftGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = UxGold600
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1.0f,
        animationSpec = MotionSpecs.TactileSpring,
        label = "softGoldScale"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .clip(RoundedCornerShape(12.dp))
            .background(UxGold100)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = UxGold600,
    iconBg: Color = UxGold100,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, UxGold200, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = UxInk)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, fontFamily = Inter, fontSize = 13.sp, color = TextSecondaryDark)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = UxGold200
        )
    }
}

@Composable
fun customerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Marigold,
    unfocusedBorderColor = Marigold.copy(alpha = 0.78f),
    focusedLabelColor = Marigold,
    unfocusedLabelColor = TextSecondaryDark,
    cursorColor = Marigold,
    focusedTextColor = TextPrimaryDark,
    unfocusedTextColor = TextPrimaryDark,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedPlaceholderColor = TextSecondaryDark,
    unfocusedPlaceholderColor = TextSecondaryDark,
    disabledBorderColor = Marigold.copy(alpha = 0.55f),
    disabledTextColor = TextPrimaryDark,
    disabledLabelColor = TextSecondaryDark,
    disabledContainerColor = Color(0xFFF7F3EA)
)
