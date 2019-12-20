package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.example.mediaplayer.data.MediaPreferences
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
     * will be called when the corresponding observer is remove from list of observers
     */
    fun onObserverDetach(iPlayerState: IPlayerState) {}

    /**this is called when the player  is being stopped
     */
    fun onPlayerStop() {}
}

@Suppress("UNCHECKED_CAST")
open class PlayerListenerDelegate(private val service: AudioForegroundService,
                                  private val player: SimpleExoPlayer?, private val mediaPreferences: MediaPreferences
) : CoroutineScope by CustomScope(Dispatchers.Main) {
    private lateinit var onPlayerStateChanged: Player.EventListener

    private val onPlayerStateListListeners: HashMap<IPlayerState, ArrayList<IPlayerListener>> = HashMap()
    private var currentAudioIndex = -1
    private var isPlaying = true
    private var playbackPosition = 0L
    private var durationSet: Boolean = false
    private var isPLayerPreparedBefore = false

    /**
     * to indicate if the player is released or not so when the ui is not visible we release the player
     * this is to avoid reinitializing the player again when user release the player from notification and ui
     * is still visible so if he resume the player we do not have to initialize it again
     */
    protected var isReleased = false

    companion object {
        private const val DELAY = 350L


    }

    //handle the player when actions happen in notification
    protected fun setOnPlayerStateChangedListener(observers: HashMap<IPlayerState, ArrayList<IPlayerListener>>) {
        onPlayerStateListListeners.updateList(observers)
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (player.isTrackChanging(reason)) {
                            Log.v("registeringAudioSession", " tracking  $currentWindowIndex $playWhenReady ")
                            durationSet = false
                            if (isReleased) {
                                isReleased = false
                            }
                            launch {
                                //give time for ui to prepare
                                delay(DELAY)
                                isPlaying = playWhenReady
                                currentAudioIndex = currentWindowIndex
                                onPlayerStateListListeners.forEach {
                                    it.key.onAudioChanged(currentAudioIndex, playWhenReady, player.currentTag)
                                }

                            }

                        }
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when {
                            player.isNewDurationReady() -> {

                                launch {
                                    //give time for player to prepare duration value
                                    delay(DELAY)
                                    Log.v("registeringAudioSession", " duration ${player.duration}  ")
                                    onPlayerStateListListeners.forEach {
                                        it.key.onDurationChange(player.duration)
                                    }
                                }
                                durationSet = true
                            }
                            player.isPlayerPlaying() -> {
                                Log.v("registeringAudioSession", " playing  ")
                                if (playWhenReady != isPlaying) {
                                    isPlaying = playWhenReady
                                    // Active playback.
                                    //when player start again we start listening to  events of headphone
                                    if (isReleased) {
                                        isReleased = false
                                    }
                                    onPlayerStateListListeners.forEach { entry ->
                                        entry.key.onPlay()
                                        entry.value.forEach {
                                            it.onActivePlayer()
                                        }
                                    }
                                }

                            }
                            player.isPlayerPausing() -> {
                                Log.v("registeringAudioSession", " pausing")
                                playbackPosition = currentPosition
                                if (playWhenReady != isPlaying) {
                                    isPlaying = playWhenReady
                                    // Paused by app.
                                    if (isReleased) {
                                        isReleased = false
                                    }
                                    onPlayerStateListListeners.forEach { entry ->
                                        entry.key.onPause()
                                        entry.value.forEach {
                                            it.onInActivePlayer()
                                        }
                                    }

                                }
                            }
                            ExoPlayer.STATE_IDLE == playbackState -> {
                                // Not playing because playback ended, the player is buffering, stopped or
                                // failed. Check playbackState and player.getPlaybackError for details.
                                mediaPreferences.apply {
                                    setCurrentTrack(currentAudioIndex)
                                    setCurrentPosition(playbackPosition)
                                }
                                onPlayerStateListListeners.forEach { entry ->
                                    entry.key.onStop()
                                    entry.value.forEach {
                                        it.onPlayerStop()
                                    }
                                }
                            }
                            player.isTracksEnded() -> {
                                onPlayerStateListListeners.forEach {
                                    it.key.onAudioListCompleted()

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


    /**
     * will create singleton noisy listener only one time
     */
    protected fun setNoisyListener(): IPlayerListener {
        return Noisy.create(service, EventDispatcher(service))
    }

    protected fun setAudioSessionChangeListener(updatedPlayerState: IPlayerState): IPlayerListener {
        return OnAudioSessionIdChangeListener.createOrUpdate(service, player!!, updatedPlayerState)

    }

    protected fun setOnProgressChangedListener(iPlayerState: IPlayerState): IPlayerListener {
        val onProgressChanged = OnAudioProgressChangeListener(player!!)
        iPlayerState.onProgressChangedLiveData(onProgressChanged)
        return onProgressChanged

    }

    private fun Player.isNewDurationReady(): Boolean {
        return playbackState == ExoPlayer.STATE_READY && !durationSet
    }

    private fun Player.isPlayerPausing(): Boolean {
        return !playWhenReady && playbackState == Player.STATE_READY
    }

    private fun Player.isPlayerPlaying(): Boolean {
        return playWhenReady && playbackState == Player.STATE_READY

    }

    private fun Player.isTracksEnded(): Boolean {
        return playbackState == ExoPlayer.STATE_ENDED
    }

    private fun Player.isTrackChanging(reason: Int): Boolean {
        return reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK && currentWindowIndex != currentAudioIndex
    }
}


