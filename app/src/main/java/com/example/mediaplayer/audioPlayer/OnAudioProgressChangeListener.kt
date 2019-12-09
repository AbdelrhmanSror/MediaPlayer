package com.example.mediaplayer.audioPlayer

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.SimpleExoPlayer

class OnAudioProgressChangeListener(private val player: SimpleExoPlayer) : MutableLiveData<Long>(), IpLayerState {
    /**
     * to indicate if the player is released or not so when the ui is not visible we release the player
     * this is to avoid reinitializing the player again when user release the player and ui
     * is still visible so if he resume the player we do not have to initialize it again
     */
    private var isReleased = false

    //to see if the ui is visible or not
    private var isUiVisible = true

    private lateinit var runnable: Runnable

    private var handler: Handler? = null


    override fun onPlay() {
        isReleased = false
        if (isUiVisible) {
            Log.v("progresschanging", "resying")
            resumeProgress()
        }
    }

    override fun onPause() {
        isReleased = false
        Log.v("progresschanging", "pausing")
        pauseProgress()

    }

    override fun onDeattached() {
        Log.v("progressActivity", "deattached")
        stopProgress()
    }

    /**
     *we do not update isUiVisisble here cause this variable
     * is actually represent the actual state of ui if its already visible or not visible
     * which means that ui is completely destroyed
     * so that is why we do not update this variable here because if the screen goes black
     * and user dismiss the notification the player would be released and we don not want this
     */
    override fun onInactive() {
        Log.v("progresschanging", "inactive")
        isUiVisible = false
        pauseProgress()

    }

    override fun onActive() {
        Log.v("progressActivity", "active")

        isUiVisible = true
        if (!::runnable.isInitialized) {
            handler = Handler()
            runnable = Runnable {
                player.let {
                    // onPlayerStateChanged.onProgressChanged(player!!.currentPosition)
                    postValue(player.currentPosition)
                    //update the text position under seek bar to reflect the current position of seek bar
                    handler?.postDelayed(runnable, 50)
                }
            }
            handler?.postDelayed(runnable, 50)
        } else
            resumeProgress()
    }

    private fun pauseProgress() {
        if (::runnable.isInitialized) {
            handler?.removeCallbacks(runnable)
        }
    }

    private fun resumeProgress() {
        if (::runnable.isInitialized) {
            handler?.post(runnable)
        }
    }


    /**
     * this will only be called if the ui is completely destroyed
     */
    private fun stopProgress() {
        pauseProgress()
        handler = null
    }

}