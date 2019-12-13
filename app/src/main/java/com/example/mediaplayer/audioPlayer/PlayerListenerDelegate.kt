package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.shared.CustomScope
import com.example.mediaplayer.shared.updateList
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


interface IPlayerListener<T> {


    /**
     * this will be called when there is audio playing
     */
    fun onActivePlayer() {}

    /**
     * this will be called when audio paused
     * is also called when the player is stopped which mean player is completely destroyed
     */
    fun onInActivePlayer(isStopped: Boolean) {}

    /**
     * will be called when there corresponding observer is remove from list of observers
     */
    fun onDetach(iPlayerState: IPlayerState<T>) {}
}

@Suppress("UNCHECKED_CAST")
open class PlayerListenerDelegate<T>(private val service: AudioForegroundService,
                                     private val player: SimpleExoPlayer?
) : CoroutineScope by CustomScope(Dispatchers.Main) {
    private lateinit var onPlayerStateChanged: Player.EventListener

    private val onPlayerStateListListeners: HashMap<IPlayerState<T>, ArrayList<IPlayerListener<T>>> = HashMap()
    private var isPlaying = true

    private var currentAudioIndex = -1

    private var durationSet: Boolean = false
    private var isPLayerPreparedBefore = false

    /**
     * to indicate if the player is released or not so when the ui is not visible we release the player
     * this is to avoid reinitializing the player again when user release the player from notification and ui
     * is still visible so if he resume the player we do not have to initialize it again
     */
    protected var isReleased = false


    //handle the player when actions happen in notification
    protected fun setOnPlayerStateChangedListener(observers: HashMap<IPlayerState<T>, ArrayList<IPlayerListener<T>>>) {
        onPlayerStateListListeners.updateList(observers)
        //onPlayerStateListListeners.addAll(ipLayerState)
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (player.isTrackChanging(reason)) {
                            currentAudioIndex = currentWindowIndex
                            durationSet = false
                            if (isReleased) {
                                isReleased = false
                            }
                            launch {
                                //give time for ui to prepare
                                delay(350)
                                Log.v("registeringAudioSession", " tracking  $currentWindowIndex $isPlaying ")
                                onPlayerStateListListeners.forEach {
                                    it.key.onAudioChanged(currentAudioIndex, isPlaying, player.currentTag as T)
                                }

                            }

                        }
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when {
                            player.isNewDurationReady() -> {

                                launch {
                                    //give time for player to prepare duration value
                                    delay(300)
                                    Log.v("registeringAudioSession", " duration ${player.duration}  ")
                                    onPlayerStateListListeners.forEach {
                                        it.key.onDurationChange(player.duration)
                                    }
                                }
                                durationSet = true
                            }
                            player.isPlayerPlaying() -> {
                                Log.v("registeringAudioSession", " playing  ")
                                // Active playback.
                                //when player start again we start listening to  events of headphone
                                if (isReleased) {
                                    isReleased = false
                                }
                                isPlaying = true
                                onPlayerStateListListeners.forEach { entry ->
                                    entry.key.onPlay()
                                    entry.value.forEach {
                                        it.onActivePlayer()
                                    }
                                }


                            }
                            ExoPlayer.STATE_IDLE == playbackState && isPLayerPreparedBefore -> {
                                // Not playing because playback ended, the player is buffering, stopped or
                                // failed. Check playbackState and player.getPlaybackError for details.
                                Log.v("registeringAudioSession", " stopping ${playbackState}")

                            }
                            player.isTracksEnded() -> {
                                onPlayerStateListListeners.forEach {
                                    it.key.onAudioListCompleted()

                                }

                            }
                            player.isPlayerPausing() -> {
                                Log.v("registeringAudioSession", " pausing")
                                if (playWhenReady != isPlaying) {
                                    // Paused by app.
                                    isPlaying = false
                                    if (isReleased) {
                                        isReleased = false
                                    }
                                    onPlayerStateListListeners.forEach { entry ->
                                        entry.key.onPause()
                                        entry.value.forEach {
                                            it.onInActivePlayer(false)
                                        }
                                    }

                                }
                            }
                        }
                    }


                    override fun onRepeatModeChanged(repeatMode: Int) {
                        onPlayerStateListListeners.forEach {
                            it.key.onRepeatModeChanged(repeatMode)
                        }
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        onPlayerStateListListeners.forEach {
                            it.key.onShuffleModeChanged(shuffleModeEnabled)
                        }

                    }
                }
                addListener(onPlayerStateChanged)
            }
        }
    }


    protected fun removeListeners() {
        player?.addAudioListener(null)

    }


    /**
     * will create singleton noisy listener only one time
     */
    protected fun setNoisyListener(): IPlayerListener<T> {
        return Noisy.create(service, EventDispatcher(service))
    }

    protected fun setAudioSessionChangeListener(updatedPlayerState: IPlayerState<T>): IPlayerListener<T> {
        return OnAudioSessionIdChangeListener.createOrUpdate(service, player!!, updatedPlayerState)

    }

    protected fun setOnProgressChangedListener(iPlayerState: IPlayerState<T>): IPlayerListener<T> {
        val onProgressChanged = OnAudioProgressChangeListener<T>(player!!)
        iPlayerState.onProgressChangedLiveData(onProgressChanged)
        return onProgressChanged

    }

    private fun Player.isNewDurationReady(): Boolean {
        return playbackState == ExoPlayer.STATE_READY && !durationSet
    }

    private fun Player.isPlayerPausing(): Boolean {
        return !playWhenReady && playbackState == Player.STATE_READY && playWhenReady != isPlaying
    }

    private fun Player.isPlayerPlaying(): Boolean {
        return playWhenReady && playbackState == Player.STATE_READY && playWhenReady != isPlaying

    }

    private fun Player.isTracksEnded(): Boolean {
        return playbackState == ExoPlayer.STATE_ENDED
    }

    private fun Player.isTrackChanging(reason: Int): Boolean {
        return reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK && currentWindowIndex != currentAudioIndex
    }
}


