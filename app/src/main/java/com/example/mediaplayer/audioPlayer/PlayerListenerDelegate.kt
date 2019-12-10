package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.updateList
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer


interface IPlayerListener {

    /**
     * this will be called when there is audio playing
     */
    fun onActivePlayer() {}

    /**
     * this will be called when audio paused
     */
    fun onInActivePlayer() {}

    /**
     * will be called when there corresponding observer is remove from list of observers
     */
    fun onDetach(iPlayerState: IPlayerState)
}

open class PlayerListenerDelegate(private val service: AudioForegroundService,
                                  private val player: SimpleExoPlayer?
) {
    private lateinit var onPlayerStateChanged: Player.EventListener

    private val onPlayerStateListListeners: HashMap<IPlayerState, ArrayList<IPlayerListener>> = HashMap()
    private lateinit var onProgressChanged: OnAudioProgressChangeListener

    private lateinit var noisy: Noisy
    private var isPlaying = true

    private var currentAudioIndex = -1

    private var durationSet: Boolean = false


    //handle the player when actions happen in notification
    protected fun setOnPlayerStateChangedListener(observers: HashMap<IPlayerState, ArrayList<IPlayerListener>>) {
        onPlayerStateListListeners.updateList(observers)
        //onPlayerStateListListeners.addAll(ipLayerState)
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (player.isTrackChanging(reason)) {
                            Log.v("playbackstakestate", " tracking  ")
                            currentAudioIndex = currentWindowIndex
                            durationSet = false
                            onPlayerStateListListeners.forEach {
                                it.key.onAudioChanged(currentWindowIndex, playWhenReady)
                            }

                        }
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when {
                            player.isNewDurationReady() -> {
                                Log.v("playbackstakestate", " duration  ")

                                onPlayerStateListListeners.forEach {
                                    it.key.onDurationChange(player.duration)
                                }
                                durationSet = true
                            }
                            player.isPlayerPlaying() -> {
                                Log.v("playbackstakestate", " playing  ")
                                // Active playback.
                                //when player start again we start listening to  events of headphone

                                isPlaying = true
                                onPlayerStateListListeners.forEach { entry ->
                                    entry.key.onPlay()
                                    entry.value.forEach {
                                        it.onActivePlayer()
                                    }
                                }


                            }
                            playWhenReady -> {
                                // Not playing because playback ended, the player is buffering, stopped or
                                // failed. Check playbackState and player.getPlaybackError for details.
                                Log.v("playbackstakestate", " stopping ")
                            }
                            player.isTracksEnded() -> {
                                onPlayerStateListListeners.forEach {
                                    it.key.onAudioListCompleted()

                                }

                            }
                            player.isPlayerPausing() -> {
                                Log.v("playbackstakestate", " pausing")
                                if (playWhenReady != isPlaying) {
                                    // Paused by app.
                                    isPlaying = false
                                    onPlayerStateListListeners.forEach { entry ->
                                        entry.key.onPause()
                                        entry.value.forEach {
                                            it.onInActivePlayer()
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
        player?.addListener(null)
        player?.addAudioListener(null)

    }

    /**
     * will create singleton noisy listener only one time
     */
    protected fun setNoisyListener(): IPlayerListener {
        if (!::noisy.isInitialized) {
            noisy = Noisy(service, EventDispatcher(service))
        }
        return noisy
    }

    protected fun setAudioSessionChangeListener(updatedPlayerState: IPlayerState): IPlayerListener {
        return OnAudioSessionIdChangeListener.createOrUpdate(player!!, updatedPlayerState)

    }

    protected fun setOnProgressChangedListener(iPlayerState: IPlayerState): IPlayerListener {
        onProgressChanged = OnAudioProgressChangeListener.create(player!!)
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


