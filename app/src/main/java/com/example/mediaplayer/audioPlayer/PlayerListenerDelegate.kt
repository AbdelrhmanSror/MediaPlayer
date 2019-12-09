package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.updateList
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer

/*
private interface IPlayerListener {
    fun setOnPlayerStateChangedListener(ipLayerState: Collection<IpLayerState?>)
    fun setAudioSessionChangeListener(ipLayerState: Collection<IpLayerState?>, OnAudioSessionListenerInitialized: (IpLayerState) -> Unit) {}
    fun setOnProgressChangedListener(ipLayerState: Collection<IpLayerState?>, OnProgressListenerInitialized: (IpLayerState) -> Unit) {}
    fun setNoisyListener(eventDispatcher: EventDispatcher, onNoisyInitialized: (IpLayerState) -> Unit) {}

}*/

open class PlayerListenerDelegate(private val service: AudioForegroundService,
                                  private val player: SimpleExoPlayer?
) {
    private lateinit var onPlayerStateChanged: Player.EventListener
    private var onAudioSessionIdChangeListener: OnAudioSessionIdChangeListener? = null

    private val onPlayerStateListListeners: HashSet<IpLayerState> = HashSet()
    private lateinit var onProgressChanged: OnAudioProgressChangeListener

    private lateinit var noisy: Noisy
    private var isPlaying = true

    private var currentAudioIndex = -1

    private var durationSet: Boolean = false


    //handle the player when actions happen in notification
    private fun setOnPlayerStateChangedListener(ipLayerState: Collection<IpLayerState>) {
        onPlayerStateListListeners.updateList(ipLayerState.toHashSet())
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (player.isTrackChanging(reason)) {
                            Log.v("playbackstakestate", " tracking  ")
                            currentAudioIndex = currentWindowIndex
                            durationSet = false
                            onPlayerStateListListeners.forEach {
                                it.onAudioChanged(currentWindowIndex, playWhenReady)
                            }
                        }
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when {
                            player.isNewDurationReady() -> {
                                Log.v("playbackstakestate", " duration  ")

                                onPlayerStateListListeners.forEach {
                                    it.onDurationChange(player.duration)
                                }
                                durationSet = true
                            }
                            player.isPlayerPlaying() -> {
                                Log.v("playbackstakestate", " playing  ")
                                // Active playback.
                                //when player start again we start listening to  events of headphone

                                isPlaying = true
                                onPlayerStateListListeners.forEach {
                                    it.onPlay()
                                }
                            }
                            playWhenReady -> {
                                // Not playing because playback ended, the player is buffering, stopped or
                                // failed. Check playbackState and player.getPlaybackError for details.
                                Log.v("playbackstakestate", " stopping ")
                            }
                            player.isTracksEnded() -> {
                                onPlayerStateListListeners.forEach {
                                    it.onAudioListCompleted()
                                }
                            }
                            player.isPlayerPausing() -> {
                                Log.v("playbackstakestate", " pausing")
                                if (playWhenReady != isPlaying) {
                                    // Paused by app.
                                    isPlaying = false
                                    onPlayerStateListListeners.forEach {
                                        it.onPause()
                                    }
                                }
                            }
                        }
                    }


                    override fun onRepeatModeChanged(repeatMode: Int) {
                        onPlayerStateListListeners.forEach {
                            it.onRepeatModeChanged(repeatMode)
                        }
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        onPlayerStateListListeners.forEach {
                            it.onShuffleModeChanged(shuffleModeEnabled)
                        }
                    }
                }
                addListener(onPlayerStateChanged)
            }
        }
    }

    protected fun updateListeners(iPlayerState: HashSet<IpLayerState>) {
        setOnPlayerStateChangedListener(iPlayerState)
        setOnProgressChangedListener(iPlayerState)
        setAudioSessionChangeListener(iPlayerState)

    }

    protected fun removeListeners() {
        player?.addListener(null)
        player?.addAudioListener(null)

    }

    protected fun setNoisyListener(eventDispatcher: EventDispatcher, onNoisyInitialized: (IpLayerState) -> Unit) {
        if (!::noisy.isInitialized) {
            noisy = Noisy(service, eventDispatcher)
            onNoisyInitialized(noisy)
        }
    }

    protected fun setAudioSessionChangeListener(ipLayerState: Collection<IpLayerState>, OnAudioSessionListenerInitialized: ((IpLayerState) -> Unit)? = null) {
        onAudioSessionIdChangeListener = OnAudioSessionIdChangeListener.createOrNullUpdate(ipLayerState.toHashSet())
        if (onAudioSessionIdChangeListener != null) {
            player!!.addAudioListener(onAudioSessionIdChangeListener)
            OnAudioSessionListenerInitialized?.invoke(onAudioSessionIdChangeListener!!)
        }
    }

    protected fun setOnProgressChangedListener(ipLayerState: Collection<IpLayerState>, OnProgressListenerInitialized: ((IpLayerState) -> Unit)? = null) {
        if (!::onProgressChanged.isInitialized) {
            onProgressChanged = OnAudioProgressChangeListener(player!!)
            OnProgressListenerInitialized?.invoke(onProgressChanged)
        }
        ipLayerState.forEach {
            it.onProgressChangedLiveData(onProgressChanged)
        }
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


