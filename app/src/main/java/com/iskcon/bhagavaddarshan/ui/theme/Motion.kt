package com.iskcon.bhagavaddarshan.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp

/** Shared motion tokens so animation feel is consistent across every screen. */
object Motion {
    val EnterTween = tween<Float>(durationMillis = 420, easing = FastOutSlowInEasing)
    val QuickTween = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    const val StaggerStepMs = 45
}

object MotionSpecs {
    val TactileSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val SmoothSlide = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
