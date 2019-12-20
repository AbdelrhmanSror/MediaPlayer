package com.example.mediaplayer.audioPlayer

import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.shared.CustomScope
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.*

class OnAudioProgressChangeListener(private val player: SimpleExoPlayer) :
        MutableLiveData<Long>(),
        IPlayerListener, CoroutineScope by CustomScope(Dispatchers.Main) {
    //to see if the ui is visible or not
    private var isUiVisible = true

    private var job: Job? = null
    override fun onInActivePlayer() {
        stopTimer()

    }

    override fun onActivePlayer() {
        if (isUiVisible) {
            startTimer()
        }

    }

    override fun onInactive() {
        isUiVisible = false
        stopTimer()

    }


    override fun onActive() {
        isUiVisible = true
        startTimer()
    }


    private fun startTimer() {
        job = launch {
            while (true) {
                postValue(player.currentPosition)
                delay(200)
            }
        }
    }

    private fun stopTimer() {
        job?.cancel()
    }

}