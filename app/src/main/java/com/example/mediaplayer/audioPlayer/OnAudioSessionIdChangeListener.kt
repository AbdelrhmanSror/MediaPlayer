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
internal class OnAudioSessionIdChangeListener private constructor(service: AudioForegroundService)
    : AudioListener,
        IPlayerListener,
        DefaultLifecycleObserver,
        CoroutineScope by MainScope() {
    companion object {
        private const val DELAY = 500L
        private var observers = HashSet<IPlayerObserver>()
        private var audioSessionId = -1
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
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener(service)
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
     * reset the audiosession when the service is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        job?.cancel()
        onAudioSessionIdChangeListener = null
    }

    /**
     * remove observer from list of observer that recieve audiosession event
     * when the current observer is removed from list of observer the observe player event
     */
    override fun onObserverDetach(iPlayerObserver: IPlayerObserver) {
        observers.remove(iPlayerObserver)
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