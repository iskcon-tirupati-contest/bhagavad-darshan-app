package com.iskcon.bhagavaddarshan.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import com.iskcon.bhagavaddarshan.R

/**
 * UI feedback sounds.
 *
 * Ordinary taps use Android's built-in [AudioManager.FX_KEY_CLICK] (no asset files).
 * Save success keeps the soft final chime. Screen transitions intentionally silent.
 */
object UiSounds {
    @Volatile private var pool: SoundPool? = null
    @Volatile private var chimeId = 0

    fun warmUp(context: Context) {
        if (pool != null) return
        synchronized(this) {
            if (pool != null) return
            val p = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
            chimeId = p.load(context.applicationContext, R.raw.sfx_chime, 1)
            pool = p
        }
    }

    /** Soft system UI tap — tabs, cards, FAB, expand/collapse. */
    fun click(context: Context) {
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            am.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.45f)
        } catch (_: Throwable) {
        }
    }

    /** Soft confirmation after a successful save (non-chime flows). */
    fun success(context: Context) = tone(ToneGenerator.TONE_PROP_ACK, 90, 45)

    /** Soft error feedback. */
    fun error(context: Context) = tone(ToneGenerator.TONE_PROP_NACK, 120, 50)

    /** Feedback after a confirmed delete (not on the initial trash tap). */
    fun delete(context: Context) = tone(ToneGenerator.TONE_PROP_PROMPT, 70, 40)

    /**
     * No-op. Screen / tab transitions stay silent — visual slide is enough.
     * Kept so older call sites compile without churn.
     */
    @Suppress("UNUSED_PARAMETER")
    fun swipe(context: Context) = Unit

    /** Final save bell (devotee saved) — quieter than before, still distinctive. */
    fun chime(context: Context) {
        try {
            warmUp(context)
            if (chimeId != 0) pool?.play(chimeId, 0.68f, 0.68f, 1, 0, 1f)
            else success(context)
        } catch (_: Throwable) {
        }
    }

    private fun tone(type: Int, durationMs: Int, volume: Int) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, volume.coerceIn(1, 100))
            tg.startTone(type, durationMs)
            // Release after the tone finishes; don't hold ToneGenerator forever.
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try { tg.release() } catch (_: Throwable) {}
            }, (durationMs + 80).toLong())
        } catch (_: Throwable) {
        }
    }
}
