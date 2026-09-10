package com.iskcon.bhagavaddarshan.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Thin wrapper around [View.performHapticFeedback] so taps/press states get a
 * subtle physical "click" alongside [UiSounds] — this pairing (sound + haptic)
 * is what makes premium apps feel tactile instead of flat.
 *
 * Safe to call from any thread-safe UI callback; failures are swallowed since
 * haptics are a nice-to-have, never something that should crash a screen.
 */
object Haptics {
    /** Light tap — buttons, cards, list rows. */
    fun tap(view: View) = perform(view, HapticFeedbackConstants.KEYBOARD_TAP)

    /** Slightly firmer tap — destructive actions (delete, logout confirm). */
    fun firm(view: View) = perform(
        view,
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.LONG_PRESS
    )

    /** Positive confirmation — save success, form submit succeeded. */
    fun success(view: View) = perform(
        view,
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.KEYBOARD_TAP
    )

    private fun perform(view: View, constant: Int) {
        try {
            view.performHapticFeedback(constant)
        } catch (_: Throwable) {
        }
    }
}
