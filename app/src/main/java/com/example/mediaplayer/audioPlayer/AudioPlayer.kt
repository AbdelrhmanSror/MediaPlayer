package com.example.mediaplayer.audioPlayer

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompat
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import javax.inject.Inject

data class AudioPlayerModel(val currentIndex: Int,
                            val isPlaying: Boolean,
                            val shuffleModeEnabled: Boolean,
                            val repeatMode: Int,
                            val duration: Long)


class AudioPlayer @Inject constructor(private val service: AudioForegroundService,
                                      private val mediaSessionCompat: MediaSessionCompat,
                                      var player: SimpleExoPlayer?, private val mediaAudioFocusCompat: MediaAudioFocusCompat,
                                      private val mediaPreferences: MediaPreferences)
    : PlayerListenerDelegate(service, player!!, mediaPreferences),
        IPlayerControl by PlayerControlDelegate(service, player, mediaAudioFocusCompat, mediaPreferences),
        AudioPlayerObservable {


    /**
     * store observers and their corresponding listeners into hash map so it could be easily to notify or remove listener when registered observer
     */
    private val observers: HashMap<IPlayerState, ArrayList<IPlayerListener>> = HashMap()


    private val mainObservers: HashSet<IPlayerState> = HashSet()

    private var isNoisyModeEnabled = false

    //to give flexibility if i want to do extra work while releasing the player like stopping service
    private var extraRelease: (() -> Unit)? = null


    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(mediaSessionCompat)
    }

    init {
        Log.v("audioManagerNotificati", "audioplayer")

    }

    /**
     * to register observer we need to give it the class that implement the interface of observer
     */
    override fun registerObserver(iPlayerState: IPlayerState, audioSessionIdCallbackEnable: Boolean
                                  , audioNoisyControlEnable: Boolean
                                  , progressCallBackEnabled: Boolean
                                  , isMainObserver: Boolean) {
        /**
         * as long as this observer is still registered as observer the app can not be released
         */
        if (isMainObserver)
            mainObservers.add(iPlayerState)
        if (!observers.containsKey(iPlayerState)) {
            //only setup noisy filter once
            if (!isNoisyModeEnabled && audioNoisyControlEnable) {
                isNoisyModeEnabled = true
                observers[iPlayerState] = getListOfListeners(audioSessionIdCallbackEnable, iPlayerState, true, progressCallBackEnabled)
            } else
                observers[iPlayerState] = getListOfListeners(audioSessionIdCallbackEnable, iPlayerState, false, progressCallBackEnabled)
            notifyObserver(iPlayerState)
            setOnPlayerStateChangedListener(observers)
        }
    }

    /**
     * get list of listeners that is registered to be triggered
     */
    private fun getListOfListeners(audioSessionIdCallbackEnable: Boolean,
                                   iPlayerState: IPlayerState, audioNoisyControlEnable: Boolean,
                                   progressCallBackEnabled: Boolean): ArrayList<IPlayerListener> {
        val listOfListeners = arrayListOf<IPlayerListener>()
        if (audioSessionIdCallbackEnable) listOfListeners.add(setAudioSessionChangeListener(iPlayerState))
        if (audioNoisyControlEnable) listOfListeners.add(setNoisyListener())
        if (progressCallBackEnabled) listOfListeners.add(setOnProgressChangedListener(iPlayerState))
        return listOfListeners
    }

    /**
     *
     * call this if u just want to un register your observer,this will not release the player
     *
     * if u want to release every thing use [release]
     *
     * if you have registered to listen to progress this [removeObserver] will stop the progress
     *
     * NOTE: this will act as [release] if there is only one observer so no need to call both together just one of them
     */
    override fun removeObserver(iPlayerState: IPlayerState) {
        //calling onDatch fun of every listenr first
        observers[iPlayerState]?.forEach {
            it.onObserverDetach(iPlayerState)
        }
        //removing the observers
        mainObservers.remove(iPlayerState)
        observers.remove(iPlayerState)
        invalidate()
        //update the observer in the playerStateChangeListener so if any event happened it will have the latest list of observers
        setOnPlayerStateChangedListener(observers)
    }

    override fun removeAllObservers() {
        //iterating over all observer and call ondetach method
        observers.keys.forEach { observer ->
            observers[observer]?.forEach { listener ->
                listener.onObserverDetach(observer)
            }
        }
        observers.clear()
    }


    override fun notifyObserver(iPlayerState: IPlayerState) {
        //only notify observer if the player at state ready
        if (player!!.isPlayerStateReady()) {
            iPlayerState.onAttached(AudioPlayerModel(
                    player!!.currentWindowIndex
                    , player!!.playWhenReady,
                    player!!.shuffleModeEnabled,
                    player!!.repeatMode,
                    player!!.duration))
        } else {
            iPlayerState.onAttached(null)
        }

    }


    private fun Player.isPlayerStateReady(): Boolean {
        return this.playbackState == ExoPlayer.STATE_READY
    }


    /**
     * to control the player through headset or google assistant
     */
    fun setCommandControl(mediaSessionCallback: MediaSessionCompat.Callback, mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        mediaSessionConnector.setPlayer(player)
        // mediaSessionConnector.mediaSession.setCallback(mediaSessionCallback)
        mediaSessionCompat.isActive = true
        val queueNavigator: TimelineQueueNavigator = object : TimelineQueueNavigator(mediaSessionCompat) {
            override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
                return mediaDescriptionCompat(windowIndex)
            }

        }
        mediaSessionConnector.setQueueNavigator(queueNavigator)
    }

    override fun getCountOfMainObservers(): Int {
        return mainObservers.size
    }

    /**
     *will release the player if its release otherwise will update the release state
     */
    override fun invalidate() {
        if (isReleased && getCountOfMainObservers() == 0) {
            releasePlayerPermanently()
        }
    }

    private fun releasePlayerPermanently() {
        removeAllObservers()
        player?.release()
        player = null
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
        extraRelease?.invoke()

    }


    /**
     * if u want to release every thing use [release]
     *
     * this has no effect if number of observer larger than 1
     *
     * this will delay the releasing of player until the observers are not attached unless user resume the player again then nothing wil happen
     *
     * this best for avoiding releasing player when ui is visible also to avoid preparing player again after that
     *
     * if u want to release player immediately call [removeAllObservers] then [invalidate]
     */
    fun release(extra: (() -> Unit)?) {
        extraRelease = extra
        player.let {
            isReleased = true
            invalidate()

        }
    }

}