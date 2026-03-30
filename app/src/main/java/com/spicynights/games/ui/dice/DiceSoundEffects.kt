package com.spicynights.games.ui.dice

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.spicynights.games.R

/**
 * Short roll + land sounds for couples dice (respect global sound toggle in UI).
 */
class DiceSoundEffects(context: Context) {

    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(attrs)
        .build()

    private var rollId: Int = 0
    private var dingId: Int = 0
    private var loaded: Boolean = false

    init {
        pool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = rollId != 0 && dingId != 0
        }
        rollId = pool.load(context, R.raw.dice_roll, 1)
        dingId = pool.load(context, R.raw.dice_ding, 1)
    }

    fun playRoll(volume: Float = 0.7f) {
        if (rollId == 0) return
        pool.play(rollId, volume, volume, 1, 0, 1f)
    }

    fun playDing(volume: Float = 0.75f) {
        if (dingId == 0) return
        pool.play(dingId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
