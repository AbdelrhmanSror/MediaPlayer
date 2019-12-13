package com.example.mediaplayer.audioPlayer

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioListener
import kotlinx.coroutines.*

/**
 *  register listeners for audio session id
 */
class OnAudioSessionIdChangeListener<T> private constructor(service: AudioForegroundService) : AudioListener,
        IPlayerListener<T>, DefaultLifecycleObserver,
        CoroutineScope by MainScope() {
    @Suppress("UNCHECKED_CAST")
    companion object {
        private const val DELAY = 500L
        private var observers = HashSet<IPlayerState<*>>()
        private var audioSessionId = -1
        private lateinit var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener<*>

        fun <T> createOrUpdate(service: AudioForegroundService, player: SimpleExoPlayer, updatedPlayerState: IPlayerState<T>): OnAudioSessionIdChangeListener<T> {
            observers.add(updatedPlayerState)
            if (audioSessionId > 0) {
                Log.v("onaduiochange", "$audioSessionId")
                updatedPlayerState.onAudioSessionId(audioSessionId)
            }
            return if (!::onAudioSessionIdChangeListener.isInitialized) {
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener<T>(service)
                player.addAudioListener(onAudioSessionIdChangeListener)
                onAudioSessionIdChangeListener as OnAudioSessionIdChangeListener<T>
            } else {
                onAudioSessionIdChangeListener as OnAudioSessionIdChangeListener<T>
            }
        }
    }

    init {
        service.lifecycle.addObserver(this)

    }

    private var job: Job? = null

    override fun onDestroy(owner: LifecycleOwner) {
        audioSessionId = 0
    }

    override fun onInActivePlayer(isStopped: Boolean) {
        //we cancel the job if the player is at stop Stage
        if (isStopped) {
            job?.cancel()
        }
    }

    override fun onDetach(iPlayerState: IPlayerState<T>) {
        Log.v("onaduiochange", "on detatc")

        observers.remove(iPlayerState)
    }

    override fun onAudioSessionId(audioSessionId: Int) {
        Log.v("onaduiochange", "on call$audioSessionId")

        Companion.audioSessionId = audioSessionId
        job?.cancel()
        job = launch {
            delay(DELAY)
            onAudioSessionIdInternal(audioSessionId)
        }
    }

    private fun onAudioSessionIdInternal(audioSessionId: Int) {
        observers.forEach {
            it.onAudioSessionId(audioSessionId)
        }
    }

}