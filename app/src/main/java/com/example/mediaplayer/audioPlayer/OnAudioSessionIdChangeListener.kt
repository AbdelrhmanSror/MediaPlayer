package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioListener
import kotlinx.coroutines.*

/**
 * class to register listeners for audio session id
 */
class OnAudioSessionIdChangeListener private constructor(

) : AudioListener,
        IPlayerListener,
        CoroutineScope by MainScope() {
    companion object {
        @JvmStatic
        private val TAG = "SM:${OnAudioSessionIdChangeListener::class.java.simpleName}"
        internal const val DELAY = 500L
        private lateinit var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener
        private var observers = HashSet<IPlayerState>()

        fun createOrUpdate(player: SimpleExoPlayer, updatedPlayerState: IPlayerState): OnAudioSessionIdChangeListener {
            observers.add(updatedPlayerState)
            return if (!::onAudioSessionIdChangeListener.isInitialized) {
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener()
                player.addAudioListener(onAudioSessionIdChangeListener)
                onAudioSessionIdChangeListener
            } else {
                onAudioSessionIdChangeListener
            }
        }


    }

    private var job: Job? = null


    override fun onDetach(iPlayerState: IPlayerState) {
        if (observers.remove(iPlayerState) && observers.isEmpty()) {
            job?.cancel()
        }
    }

    override fun onAudioSessionId(audioSessionId: Int) {
        job?.cancel()
        job = launch {
            delay(DELAY)
            onAudioSessionIdInternal(audioSessionId)
        }
    }

    private fun onAudioSessionIdInternal(audioSessionId: Int) {
        Log.v(TAG, "on audio session id changed =$audioSessionId")
        observers.forEach {
            it.onAudioSessionId(audioSessionId)
        }
    }

}