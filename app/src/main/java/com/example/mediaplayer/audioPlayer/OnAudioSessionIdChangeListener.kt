package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.example.mediaplayer.updateList
import com.google.android.exoplayer2.audio.AudioListener
import kotlinx.coroutines.*

class OnAudioSessionIdChangeListener private constructor(

) : AudioListener,
        IpLayerState,
        CoroutineScope by MainScope() {
    companion object {
        @JvmStatic
        private val TAG = "SM:${OnAudioSessionIdChangeListener::class.java.simpleName}"
        internal const val DELAY = 500L
        private lateinit var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener
        private var iPlayerState = HashSet<IpLayerState>()

        fun createOrNullUpdate(updatedPlayerState: HashSet<IpLayerState>): OnAudioSessionIdChangeListener? {
            iPlayerState.updateList(updatedPlayerState)
            return if (!::onAudioSessionIdChangeListener.isInitialized) {
                onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener()
                onAudioSessionIdChangeListener
            } else {
                null
            }
        }


    }


    private var job: Job? = null


    override fun onStop() {
        job?.cancel()
        iPlayerState.clear()
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
        iPlayerState.forEach {
            it.onAudioSessionId(audioSessionId)
        }
    }

}