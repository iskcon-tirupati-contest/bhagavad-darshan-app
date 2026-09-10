package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Editorial customer palette (active). Previous saffron/gold is in [UxPilotPalette]. */
val EditCream = Color(0xFFFCF7F1)
val EditTerracotta = Color(0xFFD36B1D)
val EditTerracottaDark = Color(0xFFB85A16)
val EditChocolate = Color(0xFF4A1A12)
/** Secondary text — darkened for elder readability on cream (was #6F5B52). */
val EditMuted = Color(0xFF3D2A22)
val EditPeach = Color(0xFFFFF4E5)
val EditPopularBg = Color(0xFFFFF8E7)
val EditPopularBorder = Color(0xFFC9A227)
val EditCard = Color(0xFFFFFBF6)
val EditMint = Color(0xFFDCFCE7)
val EditMintText = Color(0xFF166534)

/** Soft teal for dashboard devotee greeting strip. */
val DevoteeTeal = Color(0xFF0F766E)
/** Formal temple green (from brand swatch) — nav accent. */
val TempleGreen = Color(0xFF147547)
/** Soft green-tinted cream for shared top/bottom chrome bars. */
val ChromeBar = Color(0xFFEEF5F0)
val ChromeBarBorder = Color(0xFFC5D9CC)

val UxSaffron = EditTerracotta
val UxSaffronDark = EditTerracottaDark
val UxGold100 = EditPeach
val UxGold200 = Color(0xFFE8DDD0)
val UxGold400 = EditPopularBorder
val UxGold500 = EditTerracotta
val UxGold600 = EditTerracotta
val UxCream = EditCream
val UxInk = EditChocolate
val UxInkMuted = EditMuted
val UxInkFaint = EditChocolate.copy(alpha = 0.40f)
val UxSoftShadow = Color(0x334A1A12)

// Staff / Figma palette — keep marigold for admin & agent chrome
val Marigold = Color(0xFFE65100)
val MarigoldDeep = Color(0xFFBF360C)
val MarigoldLight = Color(0xFFFF8A50)
val MarigoldContainer = Color(0xFFFFE0B2)

val Saffron = Marigold
val SaffronDark = MarigoldDeep

val SacredGold = Color(0xFFF9A825)
val SacredGoldLight = Color(0xFFFFECB3)

val TempleMaroon = Color(0xFF7A1F3D)
val TempleMaroonLight = Color(0xFFF6D7E1)
val TempleMaroonDeep = Color(0xFF4E1327)

val Ivory = UxCream
val IvoryDeep = Color(0xFFFFF8F0)
val Cream = UxCream
val CreamDark = UxGold100

val Ink = UxInk
val InkSoft = Color(0xFF6D6D6D)
val Outline = UxGold200

val Leaf = Color(0xFF2E7D32)
val LeafContainer = Color(0xFFE8F5E9)
val SoftRed = Color(0xFFC62828)
val SoftRedContainer = Color(0xFFFFEBEE)
val AmberPending = Color(0xFFEF6C00)
val AmberPendingContainer = Color(0xFFFFF3E0)

val ChromeDark = UxCream
val ChromeDarkElevated = Color.White
val ChromeOnDark = UxInk

val TempleHeroBrush = Brush.linearGradient(
    colors = listOf(UxSaffronDark, UxSaffron, UxGold400)
)

val ChromeDarkBrush = Brush.verticalGradient(
    colors = listOf(UxCream, UxCream)
)

val TempleDuskBrush = Brush.verticalGradient(
    colors = listOf(UxSaffronDark, UxSaffron, UxGold400)
)

val GoldSheenBrush = Brush.horizontalGradient(
    colors = listOf(UxGold500, UxSaffron)
)

val IvoryWashBrush = Brush.verticalGradient(
    colors = listOf(UxCream, UxGold100)
)

// Customer aliases used across customer screens
val SkyBlueBackground = UxCream
val SkyBlueSurface = Color.White
val SkyBlueBorder = UxGold200

val SaffronOrange = UxSaffron
val SaffronLight = Color(0xFFE07A3A)
val TempleGold = UxGold500
val TempleGoldGlow = UxGold100
val TulsiGreen = Color(0xFF15803D)
val TulsiGreenLight = Color(0xFFDCFCE7)

val SkyGradient = listOf(UxCream, UxGold100)
val EmergedButtonGradient = listOf(UxSaffron, UxSaffronDark)
val DonateCardGradient = listOf(Color(0x14D36B1D), UxGold100)
val QuoteCardGradient = listOf(Color.White, UxGold100)

val GoldPrimary = UxGold500
val GoldLight = UxGold500
val GoldGlow = UxGold100
val IndigoDeep = UxCream
val IndigoSurface = Color.White
val IndigoBorder = UxGold200
val SpiritualSaffron = UxSaffron
val SpiritualCream = UxCream
val TextPrimaryDark = UxInk
val TextSecondaryDark = UxInkMuted
val GradientGold = EmergedButtonGradient
val GradientDarkSurface = SkyGradient
val GradientHeroCard = DonateCardGradient

// ---- Premium Agent identity — deep emerald + warm gold jewel tones ----
// Used for the Agent role's chrome (nav bar, hero header, FAB glow) so the
// ~100k field agents get a distinct, premium-feeling surface vs customer/admin.
val AgentEmeraldDeep = Color(0xFF063C2E)
val AgentEmerald = Color(0xFF0B4F3F)
val AgentEmeraldLight = Color(0xFF15806A)
val AgentGold = Color(0xFFF0D78C)
val AgentGoldDeep = Color(0xFFD9B95C)
val AgentIvory = Color(0xFFFBF7EE)

/** Soft green-tinted cream chrome — matches customer top/bottom bars. */
val AgentHeroBrush = Brush.verticalGradient(
    colors = listOf(ChromeBar, Color(0xFFF7FBF8), Color.White)
)
val AgentGoldSheenBrush = Brush.horizontalGradient(
    colors = listOf(AgentGoldDeep, AgentGold)
)

