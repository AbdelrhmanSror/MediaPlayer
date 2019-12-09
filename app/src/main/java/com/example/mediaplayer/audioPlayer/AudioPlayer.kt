package com.example.mediaplayer.audioPlayer

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
                            val duration: Long,
                            val audioSessionId: Int)


class AudioPlayer @Inject constructor(private val service: AudioForegroundService,
                                      private val mediaSessionCompat: MediaSessionCompat,
                                      var player: SimpleExoPlayer?)
    : PlayerListenerDelegate(service, player!!),
        IPlayerControl by PlayerControlDelegate(service, player),
        DefaultLifecycleObserver,
        AudioPlayerObservable {

    private var audioSessionId: Int = -1

    private val iPlayerState: HashSet<IpLayerState> = HashSet()

    //to give flexibility if i want to do extra work while releasing the player like stopping service
    private var extraRelease: (() -> Unit)? = null

    //to see if the ui is visible or not
    private var isUiVisible = true

    /**
     * to indicate if the player is released or not so when the ui is not visible we release the player
     * this is to avoid reinitializing the player again when user release the player and ui
     * is still visible so if he resume the player we do not have to initialize it again
     */
    private var isReleased = false

    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(mediaSessionCompat)
    }

    init {
        service.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        removeListeners()
    }

    companion object {
        fun create(service: AudioForegroundService,
                   player: SimpleExoPlayer?,
                   mediaSessionCompat: MediaSessionCompat): AudioPlayer {
            return AudioPlayer(service, mediaSessionCompat, player)
        }
    }


    /**
     * to register observer we need to give it the class that implement the interface of observer
     */
    override fun registerObserver(ipLayerState: IpLayerState) {
        this.iPlayerState.add(ipLayerState)
        notifyObserver(ipLayerState)
        updateListeners(iPlayerState)
    }

    /**
     * WARNING :DO NOT CALL [removeObserver] inside  [release]  OTHERWISE THERE WILL BE CRASHING
     *
     * call this if u just want to un register your observer,this will not release the player
     * if u want to release every thing use [release]
     * if you have registered to listen to progress this [removeObserver] will stop the progress
     */
    override fun removeObserver(ipLayerState: IpLayerState) {
        this.iPlayerState.remove(ipLayerState)
        ipLayerState.onDeattached()
        updateListeners(iPlayerState)
    }


    override fun notifyObserver(ipLayerState: IpLayerState) {
        if (player!!.isPlayerStateReady()) {
            ipLayerState.onAttached(AudioPlayerModel(
                    player!!.currentWindowIndex
                    , player!!.playWhenReady,
                    player!!.shuffleModeEnabled,
                    player!!.repeatMode,
                    player!!.duration,
                    player!!.audioSessionId))
        } else {
            ipLayerState.onAttached(null)
        }

    }

    private fun Player.isPlayerStateReady(): Boolean {
        return this.playbackState == ExoPlayer.STATE_READY
    }

    fun setAudioSessionChangeListener() {
        setAudioSessionChangeListener(iPlayerState) {
            registerObserver(it)
        }
    }

    fun setOnProgressChangedListener() {
        setOnProgressChangedListener(iPlayerState) {
            registerObserver(it)
        }
    }

    fun setNoisyListener() {
        setNoisyListener(EventDispatcher(service)) {
            registerObserver(it)
        }
    }


    private fun releasePlayerPermanently() {
        Log.v("playerrealsed", "done")
        extraRelease?.invoke()
        player!!.release()
        player = null
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
    }


    /**
     * if u want to release every thing use [release]
     *
     * this has no effect if the observer of progress is still attached
     *
     * this will delay the releasing of player until the observer of progress is not attached unless user resume the player again nothing wil happen
     *
     * use [release] if u have no observer for progress its effect will be executed immediately
     *
     * if there was observer of progress this will have no effect until the observer of progress get unsubscribed using [removeObserver]
     *
     *if u have progress observer and  u want to immediately release the player call [removeObserver] and [release] together
     *
     * WARNING :DO NOT CALL [removeObserver] inside  [release]  OTHERWISE THERE WILL BE CRASHING(STACK OVER FLOW)
     */
    fun release(extra: (() -> Unit)?) {
        extraRelease = extra
        player.let {
            if (!isUiVisible) {
                releasePlayerPermanently()
            } else {
                isReleased = true
            }
        }
    }


    /**
     * to control the player through headset or google assistant
     */
    fun enableCommandControl(mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        mediaSessionConnector.setPlayer(player)
        mediaSessionCompat.isActive = true
        val queueNavigator: TimelineQueueNavigator = object : TimelineQueueNavigator(mediaSessionCompat) {
            override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
                return mediaDescriptionCompat(windowIndex)
            }

        }
        mediaSessionConnector.setQueueNavigator(queueNavigator)
    }

}