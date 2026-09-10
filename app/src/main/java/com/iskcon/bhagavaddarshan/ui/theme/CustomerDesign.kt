package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared visual tokens for authenticated customer pages.
 * Matches the finished Login page: cream warmth, temple brown, saffron actions, soft gold.
 * Sizes/colors tuned for mixed-age devotees (elders + middle-aged).
 */
object BdType {
    val Eyebrow = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 1.2.sp,
        color = EditTerracotta
    )
    val PageTitle = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = EditChocolate
    )
    val PageSubtitle = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = EditMuted
    )
    val SectionTitle = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = EditChocolate
    )
    val CardTitle = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = EditChocolate
    )
    val Body = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = EditMuted
    )
    val BodyStrong = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = EditChocolate
    )
    val Label = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.3.sp,
        color = EditMuted
    )
    val Caption = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = EditMuted
    )
    val Price = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = EditChocolate
    )
    val Button = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.2.sp
    )
    val NavLabel = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp
    )
    val MagazineMonth = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,
        color = EditTerracotta
    )
    val MagazineTitle = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = EditChocolate
    )
}

object BdSpace {
    val ScreenHorizontal = 22.dp
    val ScreenTop = 8.dp
    val Section = 20.dp
    val CardPad = 16.dp
    val CardGap = 12.dp
    val BottomNavClearance = 28.dp
    val ContentMaxWidth = 560.dp
}

object BdShape {
    val Card = RoundedCornerShape(20.dp)
    val CardLarge = RoundedCornerShape(22.dp)
    val Button = RoundedCornerShape(16.dp)
    val Chip = RoundedCornerShape(999.dp)
    val Soft = RoundedCornerShape(12.dp)
}

object BdMotion {
    val SelectMs = 180
    val FaqMs = 200
    val SelectTween = tween<Float>(durationMillis = SelectMs)
    val FaqTween = tween<Float>(durationMillis = FaqMs)
}

/** Centered max-width so tablets keep phone-like proportions. */
fun Modifier.customerContentWidth(): Modifier =
    this
        .fillMaxWidth()
        .widthIn(max = BdSpace.ContentMaxWidth)
