package org.mateoUR.apprecordatorios

import android.content.Context
import android.media.MediaPlayer

// ============================================================
// SoundManager
// Reproduce sonidos de boton y notificacion sin bloquear la UI.
// Equivalente a play_sound() de Python.
// Los archivos .mp3 van en res/raw/sound_button.mp3 y
// res/raw/sound_notification.mp3
// ============================================================

object SoundManager {

    fun playButton(context: Context) {
        play(context, R.raw.sound_button)
    }

    fun playNotification(context: Context) {
        play(context, R.raw.sound_notification)
    }

    private fun play(context: Context, resId: Int) {
        try {
            val mp = MediaPlayer.create(context, resId)
            mp?.apply {
                setOnCompletionListener { release() }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
