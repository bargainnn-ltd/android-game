package com.application.eatbts.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.application.eatbts.R

/**
 * Truth-or-Dare style flip reveal ([R.raw.flip]); same asset as [com.application.eatbts.GameActivity].
 */
class FlipSoundEffects(context: Context) {

    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(attrs)
        .build()

    private var flipId: Int = 0
    private var loaded: Boolean = false

    init {
        pool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = flipId != 0
        }
        flipId = pool.load(context, R.raw.flip, 1)
    }

    fun playFlip(volume: Float = 0.85f) {
        if (!loaded || flipId == 0) return
        pool.play(flipId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
