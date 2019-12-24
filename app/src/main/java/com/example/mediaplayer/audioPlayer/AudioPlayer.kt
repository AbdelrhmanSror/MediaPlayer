package com.example.mediaplayer.audioPlayer

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import javax.inject.Inject

data class AudioPlayerModel(val currentIndex: Int,
                            val isPlaying: Boolean,
                            val shuffleModeEnabled: Boolean,
                            val repeatMode: Int,
                            val currentInstance: Any? = null)


class AudioPlayer @Inject constructor(private val service: AudioForegroundService,
                                      private val mediaSessionCompat: MediaSessionCompat,
                                      private var player: SimpleExoPlayer?,
                                      private val mediaPreferences: MediaPreferences)
    : PlayerListenerDelegate(service, player!!, mediaPreferences), DefaultLifecycleObserver,
        IPlayerControl by PlayerControlDelegate(service, player, mediaPreferences),
        AudioPlayerObservable {


    /**
     * store observers and their corresponding listeners into hash map so it could be easily to notify or remove listener when registered observer
     */
    private val observers: HashMap<IPlayerState, ArrayList<IPlayerListener>> = HashMap()


    private val mainObservers: HashSet<IPlayerState> = HashSet()

    private var isNoisyModeEnabled = false

    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(mediaSessionCompat)
    }


    init {
        service.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        releasePlayerPermanently()

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
     * if u want to release every thing use [releaseIfPossible]
     *
     * if you have registered to listen to progress this [removeObserver] will stop the progress
     *
     * NOTE: this will act as [releaseIfPossible] if there is only one observer so no need to call both together just one of them
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

    private fun triggerOnDetachCallbacks() {
        //iterating over all observer and call ondetach method
        observers.keys.forEach { observer ->
            observers[observer]?.forEach { listener ->
                listener.onObserverDetach(observer)
            }
        }
    }

    override fun removeAllObservers() {
        triggerOnDetachCallbacks()
        observers.clear()
    }


    override fun notifyObserver(iPlayerState: IPlayerState) {
        with(player!!) {
            iPlayerState.onAttached(AudioPlayerModel(
                    currentIndex(),
                    isPlaying,
                    this.shuffleModeEnabled,
                    this.repeatMode,
                    currentTag()))
        }


    }


    /**
     * to control the player through headset or google assistant
     */
    fun setCommandControl(mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        mediaSessionConnector.setPlayer(player)
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
     *will decide if its appropriate to release the player or not
     */
    override fun invalidate() {
        if (isReleased && getCountOfMainObservers() == 0 && !isPlaying) {
            service.stopSelf()

        }
    }

    /**
     * call this if u want to release the player and every thing
     * without taking in consideration the live main observers and current state of player
     *
     */
    private fun releasePlayerPermanently() {
        removeAllObservers()
        triggerStoppingCallbacks()
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
        player?.release()
        player = null
    }


    /**
     * if u want to release every thing use [releaseIfPossible]
     *
     * this has no effect if number of main observer larger than 1
     *
     * this will delay the releasing of player until the main observers are not attached unless user resume the player again then nothing wil happen
     *
     * this best for avoiding releasing player when ui is visible also to avoid preparing player again after that
     *
     * if u want to release player immediately call [removeAllObservers] or just remove the main observer then [pause] then [releaseIfPossible]
     */
    fun releaseIfPossible() {
        player.let {
            isReleased = true
            invalidate()

        }
    }

}