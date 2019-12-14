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
class OnAudioSessionIdChangeListener<T> private constructor(service: AudioForegroundService, private val player: SimpleExoPlayer) : AudioListener,
        IPlayerListener<T>, DefaultLifecycleObserver,
        CoroutineScope by MainScope() {
    @Suppress("UNCHECKED_CAST")
    companion object {
        private const val DELAY = 500L
        private var observers = HashSet<IPlayerState<*>>()
        private var audioSessionId = -1
        private var isReset = false
        private var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener<*>? = null

        fun <T> createOrUpdate(service: AudioForegroundService, player: SimpleExoPlayer, updatedPlayerState: IPlayerState<T>): OnAudioSessionIdChangeListener<T> {
            observers.add(updatedPlayerState)
            if (audioSessionId > 0) {
                updatedPlayerState.onAudioSessionId(audioSessionId)
            }
            return if (onAudioSessionIdChangeListener == null) {
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener<T>(service, player)
                player.addAudioListener(onAudioSessionIdChangeListener)
                onAudioSessionIdChangeListener as OnAudioSessionIdChangeListener<T>
            } else {
                onAudioSessionIdChangeListener as OnAudioSessionIdChangeListener<T>
            }
        }
    }

    init {
        // adding this class as observer for service
        service.lifecycle.addObserver(this)

    }

    private var job: Job? = null

    /**
     * when player become active we check if we reset everything or not if yes we add audio listener again
     */
    override fun onActivePlayer() {
        if (isReset) {
            Log.w("hiFromAudioSession", "onactiveplayer")
            isReset = false
            player.addAudioListener(onAudioSessionIdChangeListener)
        }

    }

    private fun reset() {
        isReset = true
        audioSessionId = -1
        player.removeAudioListener(onAudioSessionIdChangeListener)
        job?.cancel()
    }

    /**
     * reset the audiosession when the service is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        reset()
        onAudioSessionIdChangeListener = null
    }

    /**
     * reset the audio session when the player stop
     */
    override fun onPlayerStop() {
        reset()
    }

    /**
     * remove observer from list of observer that recieve audiosession event
     * when the current observer is removed from list of observer the observe player event
     */
    override fun onObserverDetach(iPlayerState: IPlayerState<T>) {
        observers.remove(iPlayerState)
    }

    override fun onAudioSessionId(audioSessionId: Int) {
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