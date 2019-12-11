package com.example.mediaplayer.audioPlayer

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.CustomScope
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.*

class OnAudioProgressChangeListener(private val player: SimpleExoPlayer) :
        MutableLiveData<Long>(),
        IPlayerListener, CoroutineScope by CustomScope(Dispatchers.Default) {
    //to see if the ui is visible or not
    private var isUiVisible = true

    private var job: Job? = null
    override fun onInActivePlayer(isStopped: Boolean) {
        Log.w("hiFromProgress", "inactive")
        stopTimer()

    }


    override fun onDetach(iPlayerState: IPlayerState) {
        Log.w("hiFromProgress", "deatch")
        stopTimer()
    }

    override fun onActivePlayer() {
        Log.w("hiFromProgress", "active")
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
        // pauseProgress()

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