/*
package com.example.mediaplayer.audioPlayer

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import com.example.mediaplayer.CustomScope
import com.example.mediaplayer.foregroundService.AudioForegroundService
import dev.olog.service.music.MediaButton
import kotlinx.coroutines.CoroutineScope

class MediaSessionCallback(
        private val service: AudioForegroundService,
        private val mediaButton: MediaButton

) : MediaSessionCompat.Callback(), CoroutineScope by CustomScope() {
    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        val event = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)!!
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    service.changeAudioState()
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    service.goToNext()
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    service.goToPrevious()
                }
                KeyEvent.KEYCODE_MEDIA_STOP -> {
                    service.onStop()
                }
                KeyEvent.KEYCODE_HEADSETHOOK -> mediaButton.onHeatSetHookClick()
                else -> throw IllegalArgumentException("not handled")
            }
        }

        return true
    }


}
*/
