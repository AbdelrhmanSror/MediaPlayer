package com.example.mediaplayer.audioPlayer.audioFocus

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.AudioPlayerModel
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.shared.CustomScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class FocusRequestImp @Inject constructor(private val mediaAudioFocusCompat: MediaAudioFocusCompat
                                          , service: AudioForegroundService
                                          , private val player: AudioPlayer) : IPlayerState,
        CoroutineScope by CustomScope(Dispatchers.Main), DefaultLifecycleObserver {

    private var isFocusLostAgain = false
    private var fromAudioFocus = false


    override fun onDestroy(owner: LifecycleOwner) {
        mediaAudioFocusCompat.abandonAudioFocus()
    }

    init {
        service.lifecycle.addObserver(this)
        player.registerObserver(this)
    }

    /**
     * request focus for audio player to start
     */
    private fun requestFocus() {
        mediaAudioFocusCompat.requestAudioFocus(object : AudioFocusCallBacks {
            //when the focus gained we start playing audio if it was previously running
            override fun onAudioFocusGained() {
                isFocusLostAgain = false
                launch {
                    delay(2000)
                    if (!isFocusLostAgain && fromAudioFocus) {
                        Log.v("reuestingaudiofocus", "onAudioFocusGained2")
                        player.play()
                        fromAudioFocus = false
                    }
                }


            }

            //when the focus lost we pause the player and set prevPlayerState to the current state of player
            override fun onAudioFocusLost(permanent: Boolean) {
                Log.v("reuestingaudiofocus", "onAudioFocusLost")
                fromAudioFocus = !permanent
                isFocusLostAgain = true
                player.pause()
            }
        })

    }


    override fun onAttached(audioPlayerModel: AudioPlayerModel) {
        Log.v("reuestingaudiofocus", "on attacjed request")
        requestFocus()
    }

    override fun onPlay() {
        Log.v("reuestingaudiofocus", "on play request")
        requestFocus()
    }

    override fun onPause() {
        if (!fromAudioFocus) {
            Log.v("reuestingaudiofocus", "on pause abandon")
            mediaAudioFocusCompat.abandonAudioFocus()
        }
    }

    override fun onStop() {
        Log.v("reuestingaudiofocus", "on stop abandon")
        fromAudioFocus = false
        mediaAudioFocusCompat.abandonAudioFocus()
    }
}
