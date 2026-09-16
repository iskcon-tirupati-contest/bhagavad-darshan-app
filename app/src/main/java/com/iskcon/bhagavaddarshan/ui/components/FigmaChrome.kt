package com.iskcon.bhagavaddarshan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import com.iskcon.bhagavaddarshan.ui.theme.Marigold

/** Figma: orange vertical bar + section title */
@Composable
fun AccentSectionTitle(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Marigold)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Soft filled search field — border turns marigold while focused. */
@Composable
fun SoftSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    val borderColor = if (focused) Marigold else Color(0x33C9A227)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (focused) 10.dp else 8.dp,
                shape = shape,
                spotColor = Color(0x337A1F3D),
                ambientColor = Color(0x1A3E2723)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFFFCF8), Color(0xFFF6F0E8)))
            )
            .border(1.5.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = if (focused) Marigold else Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(Marigold),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, color = Color(0xFF9E9E9E), style = MaterialTheme.typography.bodyLarge)
                }
                inner()
            }
        )
    }
}

/** Figma registration stepper: check / number / outline */
@Composable
fun FigmaRegistrationStepper(step: Int) {
    val labels = listOf("Details", "Plan", "Payment")
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val done = index < step
            val active = index == step
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .then(
                            if (done || active) Modifier.background(Marigold)
                            else Modifier.border(1.5.dp, Color(0xFFBDBDBD), CircleShape)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            "${index + 1}",
                            color = if (active) Color.White else Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        active -> MaterialTheme.colorScheme.onSurface
                        done -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> Color(0xFF9E9E9E)
                    }
                )
            }
            if (index < labels.lastIndex) {
                Box(
                    Modifier
                        .weight(0.35f)
                        .height(2.dp)
                        .background(if (index < step) Marigold else Color(0xFFE0E0E0))
                )
            }
        }
    }
}

@Composable
fun FigmaStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = Color.White,
    icon: ImageVector? = null,
    iconTint: Color = Marigold,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(if (compact) 14.dp else 16.dp)
    val wash = Brush.verticalGradient(
        listOf(
            containerColor,
            containerColor.copy(alpha = 0.92f),
            Color.White.copy(alpha = 0.55f)
        )
    )
    Box(
        modifier = modifier
            .shadow(
                elevation = if (compact) 8.dp else 12.dp,
                shape = shape,
                spotColor = Color(0x337A1F3D),
                ambientColor = Color(0x1A3E2723)
            )
            .clip(shape)
            .background(wash)
            .border(1.dp, Color(0x33C9A227), shape)
            .padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 12.dp else 16.dp
            )
    ) {
        // Left accent tick for depth
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .width(3.dp)
                .height(if (compact) 28.dp else 36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(listOf(iconTint, iconTint.copy(alpha = 0.45f)))
                )
        )
        if (compact) {
            Row(
                Modifier.padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(iconTint.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = valueColor,
                        maxLines = 1
                    )
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8D7B6E),
                        letterSpacing = 0.4.sp,
                        maxLines = 1
                    )
                }
            }
        } else {
            Column(Modifier.padding(start = 10.dp)) {
                if (icon != null) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconTint.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8D7B6E),
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
fun MetaChip(text: String, container: Color = Color(0xFFF0EBE4), content: Color = Color(0xFF424242)) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = content, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ActiveGreenChip(text: String = "Active") {
    MetaChip(text = text, container = LeafContainer, content = Leaf)
}
