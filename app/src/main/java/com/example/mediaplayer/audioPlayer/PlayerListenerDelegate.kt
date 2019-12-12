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


interface IPlayerListener {

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
    fun onDetach(iPlayerState: IPlayerState) {}
}

open class PlayerListenerDelegate(private val service: AudioForegroundService,
                                  private val player: SimpleExoPlayer?
) : CoroutineScope by CustomScope(Dispatchers.Main) {
    private lateinit var onPlayerStateChanged: Player.EventListener

    private val onPlayerStateListListeners: HashMap<IPlayerState, ArrayList<IPlayerListener>> = HashMap()
    private var isPlaying = true

    private var currentAudioIndex = -1

    private var durationSet: Boolean = false
    private var isPLayerPreparedBefore = false


    //handle the player when actions happen in notification
    protected fun setOnPlayerStateChangedListener(observers: HashMap<IPlayerState, ArrayList<IPlayerListener>>) {
        onPlayerStateListListeners.updateList(observers)
        //onPlayerStateListListeners.addAll(ipLayerState)
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (player.isTrackChanging(reason)) {
                            currentAudioIndex = currentWindowIndex
                            durationSet = false
                            launch {
                                //give time for ui to prepare
                                delay(350)
                                Log.v("registeringAudioSession", " tracking  $currentWindowIndex ")
                                onPlayerStateListListeners.forEach {
                                    it.key.onAudioChanged(currentWindowIndex, playWhenReady)
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


