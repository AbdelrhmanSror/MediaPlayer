package com.example.mediaplayer.audioPlayer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import javax.inject.Inject

class Noisy @Inject constructor(private val service: Service,
                                eventDispatcher: EventDispatcher

) : IPlayerListener {

    companion object {
        @JvmStatic
        private val TAG = "SM:${Noisy::class.java.simpleName}"
    }

    private val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private var registered: Boolean = false

    override fun onActivePlayer() {
        register()
    }

    override fun onInActivePlayer() {
        unregister()
    }

    //just for precaution
    override fun onDetach(iPlayerState: IPlayerState) {
        unregister()
    }

    private fun register() {
        if (registered) {
            Log.w(TAG, "trying to re-register")
            return
        }

        Log.v(TAG, "register")
        service.registerReceiver(receiver, noisyFilter)
        registered = true
    }

    private fun unregister() {
        if (!registered) {
            Log.w(TAG, "trying to unregister but never registered")
            return
        }

        Log.v(TAG, "unregister")
        service.unregisterReceiver(receiver)
        registered = false
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.v(TAG, "on receiver noisy broadcast")
                eventDispatcher.dispatchEvent(EventDispatcher.Event.PLAY_PAUSE)
            }

        }
    }

}
