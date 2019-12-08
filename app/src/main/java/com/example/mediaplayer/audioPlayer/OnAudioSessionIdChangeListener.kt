package com.example.mediaplayer.audioPlayer

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.audio.AudioListener
import kotlinx.coroutines.*
import javax.inject.Inject

internal class OnAudioSessionIdChangeListener @Inject constructor(
        lifecycle: Lifecycle

) : AudioListener,
        DefaultLifecycleObserver,
        CoroutineScope by MainScope() {

    companion object {
        @JvmStatic
        private val TAG = "SM:${OnAudioSessionIdChangeListener::class.java.simpleName}"
        internal const val DELAY = 500L
    }

    private var job: Job? = null

    private val hash by lazy { hashCode() }

    init {
        lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        job?.cancel()
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

    }

    fun release() {
        Log.v(TAG, "onDestroy")

    }
}