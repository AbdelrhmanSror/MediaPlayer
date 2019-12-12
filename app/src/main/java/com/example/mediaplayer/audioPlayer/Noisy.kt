package com.example.mediaplayer.audioPlayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.foregroundService.AudioForegroundService

class Noisy private constructor(private val service: AudioForegroundService,
                                eventDispatcher: EventDispatcher

) : IPlayerListener, DefaultLifecycleObserver {

    companion object {
        @JvmStatic
        private val TAG = "SM:${Noisy::class.java.simpleName}"
        private lateinit var noisy: Noisy

        /**
         * will create singleton noisy listener only one time
         */
        fun create(service: AudioForegroundService, eventDispatcher: EventDispatcher): IPlayerListener {
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
        Log.w("hiFromNoisy", "trying to re-register")
        register()
    }

    override fun onInActivePlayer(isStopped: Boolean) {
        Log.w("hiFromNoisy", "trying to unregister")
        unregister()
    }


    /**
     * BAD: do not do it otherwise will unregister the noisy every time observer is removed
     * and will lead to cancel the noisy and we do not want this
     * we want to unregister the noisy every time player is being playing or pausing
     * so the best place to do this is @[onInActivePlayer] and @[onActivePlayer]
     * @[onInActivePlayer] is also called when the player is stopped which mean player is completely destroyed

     */
    /*override fun onDetach(iPlayerState: IPlayerState) {
        unregister()
    }*/

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
