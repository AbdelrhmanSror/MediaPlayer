/*
package com.example.mediaplayer.audioPlayer

import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.shared.CustomScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class MediaSessionCallback @Inject constructor(
        private val service: AudioForegroundService,
        private val mediaButton: MediaButton

) : MediaSessionCompat.Callback(), CoroutineScope by CustomScope() {

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        val event = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)!!
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    Log.v("nothandlingexception","${event.keyCode}")
                    service.changeAudioState()
                }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    Log.v("nothandlingexception","${event.keyCode}")

                    service.changeAudioState()
                }
                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    Log.v("nothandlingexception","${event.keyCode}")

                    service.changeAudioState()
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    Log.v("nothandlingexception","${event.keyCode}")

                    service.goToNext()
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    Log.v("nothandlingexception","${event.keyCode}")

                    service.goToPrevious()
                }
                KeyEvent.KEYCODE_MEDIA_STOP -> {
                    Log.v("nothandlingexception","${event.keyCode}")

                   // service.releasePlayer()
                }
                KeyEvent.KEYCODE_HEADSETHOOK -> mediaButton.onHeatSetHookClick()
                else -> Log.v("nothandlingexception","${event.keyCode}")
            }
        }

        return true
    }


}
*/
