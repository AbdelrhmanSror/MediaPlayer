package com.example.mediaplayer.audioPlayer

import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.shared.CustomScope
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.*

class OnAudioProgressChangeListener<T>(private val player: SimpleExoPlayer) :
        MutableLiveData<Long>(),
        IPlayerListener<T>, CoroutineScope by CustomScope(Dispatchers.Main) {
    //to see if the ui is visible or not
    private var isUiVisible = true

    private var job: Job? = null
    override fun onInActivePlayer() {
        stopTimer()

    }

    override fun onObserverDetach(iPlayerState: IPlayerState<T>) {
        stopTimer()
    }

    override fun onActivePlayer() {
        if (isUiVisible) {
            startTimer()
        }

    }
    /**
     *we do not update isUiVisisble here cause this variable
     * is actually represent the actual state of ui if its already visible or not visible
     * which means that ui is completely destroyed
     * so that is why we do not update this variable here because if the screen goes black
     * and user dismiss the notification the player would be released and we don not want this
     */
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