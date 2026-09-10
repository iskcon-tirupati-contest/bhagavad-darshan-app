package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.R

/** Previous heading serif — kept so we can revert in one line below. */
private val CormorantGaramondFamily = FontFamily(
    Font(R.font.cormorant_garamond_medium, FontWeight.Medium),
    Font(R.font.cormorant_garamond_semibold, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond_bold, FontWeight.Bold)
)

/** Formal book serif for titles (not ornamental). */
private val SourceSerif4Family = FontFamily(
    Font(R.font.source_serif_4_regular, FontWeight.Normal),
    Font(R.font.source_serif_4_medium, FontWeight.Medium),
    Font(R.font.source_serif_4_semibold, FontWeight.SemiBold),
    Font(R.font.source_serif_4_bold, FontWeight.Bold)
)

/**
 * Active heading serif for the whole app.
 * To revert to the previous look: change to `CormorantGaramondFamily`.
 */
val AppSerif = SourceSerif4Family

/** True Cormorant face (login “Hare Krishna” etc.) — not remapped to AppSerif. */
val CormorantFace = CormorantGaramondFamily

/** Modern functional UI — matches banner sans. */
val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

/** Legacy names kept for existing screens — both map to the active pair. */
val CormorantGaramond = AppSerif
val PlayfairDisplay = AppSerif
val Inter = Montserrat
val SourceSerif4 = SourceSerif4Family

/**
 * AppSerif (Source Serif 4) for headings, Montserrat for UI/body.
 */
val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp
    )
)

val CustomTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
)
