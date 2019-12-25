package com.example.mediaplayer.audioPlayer

import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.shared.CustomScope
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.*

internal class OnAudioProgressChangeListener(private val player: SimpleExoPlayer) :
        MutableLiveData<Long>(),
        IPlayerListener, CoroutineScope by CustomScope(Dispatchers.Main) {

    private var job: Job? = null


    override fun onInactive() {
        job?.cancel()

    }


    override fun onActive() {
        startTimer()
    }


    private fun startTimer() {
        job?.cancel()
        job = launch {
            while (true) {
                postValue(player.currentPosition)
                delay(100)
            }
        }
    }


}