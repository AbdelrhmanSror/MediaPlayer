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
class OnAudioSessionIdChangeListener private constructor(service: AudioForegroundService,
                                                         private val player: SimpleExoPlayer)
    : AudioListener,
        IPlayerListener, DefaultLifecycleObserver,
        CoroutineScope by MainScope() {
    @Suppress("UNCHECKED_CAST")
    companion object {
        private const val DELAY = 500L
        private var observers = HashSet<IPlayerState>()
        private var audioSessionId = -1
        private var isReset = false
        private var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener? = null

        fun createOrUpdate(service: AudioForegroundService, player: SimpleExoPlayer, updatedPlayerState: IPlayerState): OnAudioSessionIdChangeListener {
            observers.add(updatedPlayerState)
            if (audioSessionId > 0) {
                updatedPlayerState.onAudioSessionId(audioSessionId)
            }
            return if (onAudioSessionIdChangeListener == null) {
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener(service, player)
                player.addAudioListener(onAudioSessionIdChangeListener)
                onAudioSessionIdChangeListener as OnAudioSessionIdChangeListener
            } else {
                onAudioSessionIdChangeListener as OnAudioSessionIdChangeListener
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
        Log.v("registeringAudioSession", "on active player1")
        if (isReset) {
            Log.v("registeringAudioSession", "on active player2")
            isReset = false
            player.addAudioListener(onAudioSessionIdChangeListener)
        }

    }

    /**
     * reset the audiosession when the service is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        // reset()
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
    override fun onObserverDetach(iPlayerState: IPlayerState) {
        observers.remove(iPlayerState)
    }

    private fun reset() {
        isReset = true
        audioSessionId = -1
        player.removeAudioListener(onAudioSessionIdChangeListener)
        job?.cancel()
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