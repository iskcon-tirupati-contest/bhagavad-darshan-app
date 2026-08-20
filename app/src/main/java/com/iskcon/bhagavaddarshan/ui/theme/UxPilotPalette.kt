package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Archived UX Pilot customer theme (saffron + gold).
 *
 * Active customer colors now live in [Color] (terracotta editorial).
 * To restore this look, copy these values back onto the `Ux*` vals in Color.kt:
 *
 *   UxSaffron = UxPilotSaffron
 *   UxSaffronDark = UxPilotSaffronDark
 *   UxGold100 = UxPilotGold100
 *   UxGold200 = UxPilotGold200
 *   UxGold400 = UxPilotGold400
 *   UxGold500 = UxPilotGold500
 *   UxGold600 = UxPilotGold600
 *   UxCream = UxPilotCream
 *   UxInk = UxPilotInk
 */
object UxPilotPalette {
    val Saffron = Color(0xFFD95300)
    val SaffronDark = Color(0xFFBF4700)
    val Gold100 = Color(0xFFFBF6EC)
    val Gold200 = Color(0xFFF2E3C6)
    val Gold400 = Color(0xFFC9A04F)
    val Gold500 = Color(0xFFB8860B)
    val Gold600 = Color(0xFF9A7209)
    val Cream = Color(0xFFFFFDF2)
    val Ink = Color(0xFF3C1E0A)
}

val UxPilotSaffron = UxPilotPalette.Saffron
val UxPilotSaffronDark = UxPilotPalette.SaffronDark
val UxPilotGold100 = UxPilotPalette.Gold100
val UxPilotGold200 = UxPilotPalette.Gold200
val UxPilotGold400 = UxPilotPalette.Gold400
val UxPilotGold500 = UxPilotPalette.Gold500
val UxPilotGold600 = UxPilotPalette.Gold600
val UxPilotCream = UxPilotPalette.Cream
val UxPilotInk = UxPilotPalette.Ink
