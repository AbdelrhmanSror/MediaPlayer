package com.example.mediaplayer.audioPlayer

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.data.MediaPreferences
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import javax.inject.Inject

data class AudioPlayerModel(val currentIndex: Int,
                            val isPlaying: Boolean,
                            val shuffleModeEnabled: Boolean,
                            val repeatMode: Int,
                            val currentInstance: Any?)


class AudioIPlayer @Inject constructor(private val service: AudioForegroundService,
                                       private val mediaSessionCompat: MediaSessionCompat,
                                       private var player: SimpleExoPlayer?,
                                       private val mediaPreferences: MediaPreferences)
    : PlayerListenerDelegate(service, player!!, mediaPreferences),
        DefaultLifecycleObserver,
        IPlayerControl by PlayerControlDelegate(service, player, mediaPreferences),
        IPlayerObservable {


    /**
     * store observers and their corresponding listeners into hash map so it could be easily to notify or remove listener when registered observer
     */
    private val observers: HashMap<IPlayerObserver, ArrayList<IPlayerListener>> = HashMap()


    private val mainObservers: HashSet<IPlayerObserver> = HashSet()

    private var isNoisyModeEnabled = false

    /**
     * to give flexibility if i want to do extra work while releasing the player
     * if u want do any thing from service while releasing the player u could set  [extraRelease]
     * so when player has got released this [extraRelease] will be called
     */
    @Suppress
    var extraRelease: (() -> Unit)? = null

    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(mediaSessionCompat)
    }


    init {
        service.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.v("playerstage", "ondestoy")
        releasePlayerPermanently()

    }

    override fun registerObservers(vararg iPlayerObserver: IPlayerObserver) {
        iPlayerObserver.forEach {
            registerObserver(it)
        }
    }

    /**
     * to register observer we need to give it the class that implement the interface of observer
     */
    override fun registerObserver(iPlayerObserver: IPlayerObserver, audioSessionIdCallbackEnable: Boolean
                                  , audioNoisyControlEnable: Boolean
                                  , progressCallBackEnabled: Boolean
                                  , isMainObserver: Boolean) {
        /**
         * as long as this observer is still registered as observer the app can not be released
         */
        if (isMainObserver)
            mainObservers.add(iPlayerObserver)
        if (!observers.containsKey(iPlayerObserver)) {
            //only setup noisy filter once
            if (!isNoisyModeEnabled && audioNoisyControlEnable) {
                isNoisyModeEnabled = true
                observers[iPlayerObserver] = getListOfListeners(audioSessionIdCallbackEnable, iPlayerObserver, true, progressCallBackEnabled)
            } else
                observers[iPlayerObserver] = getListOfListeners(audioSessionIdCallbackEnable, iPlayerObserver, false, progressCallBackEnabled)
            notifyObserver(iPlayerObserver)
            setOnPlayerStateChangedListener(observers)
        }
    }

    /**
     * get list of listeners that is registered to be triggered
     */
    private fun getListOfListeners(audioSessionIdCallbackEnable: Boolean,
                                   iPlayerObserver: IPlayerObserver, audioNoisyControlEnable: Boolean,
                                   progressCallBackEnabled: Boolean): ArrayList<IPlayerListener> {
        val listOfListeners = arrayListOf<IPlayerListener>()
        if (audioSessionIdCallbackEnable) listOfListeners.add(setAudioSessionChangeListener(iPlayerObserver))
        if (audioNoisyControlEnable) listOfListeners.add(setNoisyListener())
        if (progressCallBackEnabled) listOfListeners.add(setOnProgressChangedListener(iPlayerObserver))
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
    override fun removeObserver(iPlayerObserver: IPlayerObserver) {
        //calling onDatch fun of every listenr first
        observers[iPlayerObserver]?.forEach {
            it.onObserverDetach(iPlayerObserver)
        }
        //removing the observers
        mainObservers.remove(iPlayerObserver)
        observers.remove(iPlayerObserver)
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


    override fun notifyObserver(iPlayerObserver: IPlayerObserver) {
        Log.v("registeringAudioSession", " on attach")
        with(player!!) {
            iPlayerObserver.onAttached(AudioPlayerModel(
                    currentIndex(),
                    isPlaying,
                    shuffleModeEnabled,
                    repeatMode,
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
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
        extraRelease?.invoke()
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
     * if u want to release player immediately call [removeAllObservers] or just remove the main observers then [pause] then [releaseIfPossible]
     */
    fun releaseIfPossible() {
        player.let {
            isReleased = true
            invalidate()

        }
    }

}