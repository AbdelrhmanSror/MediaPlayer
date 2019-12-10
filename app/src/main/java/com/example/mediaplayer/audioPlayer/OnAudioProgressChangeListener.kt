package com.example.mediaplayer.audioPlayer

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.SimpleExoPlayer

class OnAudioProgressChangeListener private constructor(private val player: SimpleExoPlayer) : MutableLiveData<Long>(), IPlayerListener {
    //to see if the ui is visible or not
    private var isUiVisible = true

    private lateinit var runnable: Runnable

    private var handler: Handler? = null

    override fun onInActivePlayer() {
        pauseProgress()
    }


    override fun onDetach(iPlayerState: IPlayerState) {
        pauseProgress()
        handler = null
    }

    override fun onActivePlayer() {
        if (isUiVisible) {
            resumeProgress()
        }

    }

    //singleton creation of object
    companion object {
        private lateinit var onAudioProgressChangeListener: OnAudioProgressChangeListener
        fun create(player: SimpleExoPlayer): OnAudioProgressChangeListener {
            return if (!::onAudioProgressChangeListener.isInitialized) {
                onAudioProgressChangeListener = OnAudioProgressChangeListener(player)
                onAudioProgressChangeListener
            } else {
                onAudioProgressChangeListener
            }
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
        pauseProgress()

    }

    override fun onActive() {
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


}