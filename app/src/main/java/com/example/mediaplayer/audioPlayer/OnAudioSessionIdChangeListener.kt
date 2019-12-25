package com.example.mediaplayer.audioPlayer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioListener
import kotlinx.coroutines.*

/**
 *  register listeners for audio session id
 */
internal class OnAudioSessionIdChangeListener private constructor(service: AudioForegroundService,
                                                                  private val player: SimpleExoPlayer)
    : AudioListener,
        IPlayerListener,
        DefaultLifecycleObserver,
        CoroutineScope by MainScope() {
    companion object {
        private const val DELAY = 500L
        private var observers = HashSet<IPlayerObserver>()
        private var audioSessionId = -1
        private var isReset = false
        private var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener? = null
        fun createOrUpdate(service: AudioForegroundService,
                           player: SimpleExoPlayer,
                           updatedPlayerObserver: IPlayerObserver)
                : OnAudioSessionIdChangeListener {
            observers.add(updatedPlayerObserver)
            if (audioSessionId > 0) {
                updatedPlayerObserver.onAudioSessionId(audioSessionId)
            }
            return if (onAudioSessionIdChangeListener == null) {
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener(service, player)
                player.addAudioListener(onAudioSessionIdChangeListener)
                onAudioSessionIdChangeListener!!
            } else {
                onAudioSessionIdChangeListener!!
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
            isReset = false
            player.addAudioListener(onAudioSessionIdChangeListener)
        }

    }

    /**
     * reset the audiosession when the service is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
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
    override fun onObserverDetach(iPlayerObserver: IPlayerObserver) {
        observers.remove(iPlayerObserver)
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