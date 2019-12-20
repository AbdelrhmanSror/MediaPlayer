package com.example.mediaplayer.audioPlayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.foregroundService.AudioForegroundService

@Suppress("UNCHECKED_CAST")
class Noisy private constructor(private val service: AudioForegroundService,
                                eventDispatcher: EventDispatcher

) : IPlayerListener, DefaultLifecycleObserver {

    companion object {
        private lateinit var noisy: Noisy

        /**
         * will create singleton noisy listener only one time
         */
        fun create(service: AudioForegroundService, eventDispatcher: EventDispatcher): Noisy {
            if (!::noisy.isInitialized) {
                noisy = Noisy(service, eventDispatcher)
            }
            return noisy
        }

    }

    private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private var registered: Boolean = false

    init {
        service.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        unregister()
    }


    override fun onActivePlayer() {
        register()
    }

    override fun onPlayerStop() {
        unregister()
    }

    override fun onInActivePlayer() {
        unregister()
    }


    private fun register() {
        if (registered) {
            return
        }
        service.registerReceiver(receiver, noisyFilter)
        registered = true
    }

    private fun unregister() {
        if (!registered) {
            return
        }
        service.unregisterReceiver(receiver)
        registered = false
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                eventDispatcher.dispatchEvent(EventDispatcher.Event.PLAY_PAUSE)
            }

        }
    }

}
