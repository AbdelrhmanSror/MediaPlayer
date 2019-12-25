package com.example.mediaplayer.audioPlayer

import android.util.Log
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.extensions.*
import com.example.mediaplayer.shared.CustomScope
import com.example.mediaplayer.shared.update
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


interface IPlayerListener {
    /**
     * this will be called when there is audio playing
     *
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

    /**
     * this is called when the player  is being stopped
     * will  be called immediately if stopped through google assistant
     * because we actually do not stop the player when receiving stop intent (if the ui visible) from notification for sake of re preparing player again
     */
    fun onPlayerStop() {}
}

open class PlayerListenerDelegate(private val service: AudioForegroundService,
                                  private val player: SimpleExoPlayer?,
                                  private val mediaPreferences: MediaPreferences
) : CoroutineScope by CustomScope(Dispatchers.Main) {
    private lateinit var onPlayerStateChanged: Player.EventListener

    private val onPlayerStateListListeners: HashMap<IPlayerState, ArrayList<IPlayerListener>> = HashMap()
    private var currentAudioIndex = -1
    var isPlaying = true
        private set
    private var currentInstance: Any? = null
    private var playbackPosition = 0L
    //to prevent the callback of duration to be called more than once at a time
    private var durationHandled: Boolean = false
    //to prevent the callback of track ended to be called more than once at a time
    private var trackEndHandled = false
    //to prevent the callback of track stopped to be called more than once at a time
    private var stoppingHandled = false
    private var isFirstTime: Boolean = true
    private var isStopped = false

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
        onPlayerStateListListeners.update(observers)
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (isTrackChanged(currentAudioIndex, reason)) handleTrackChanged()
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when {
                            isNewDurationReady(durationHandled) -> handleDurationChanging()
                            isPlayerPlaying() -> handlePlayerPlaying(playWhenReady)
                            isPlayerPausing() -> handlePlayerPausing(playWhenReady)
                            isPlayerStopping() -> handlePlayerStopping()
                            isTracksEnded(trackEndHandled) -> handlePlayerTracksEnded()
                        }
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {
                        triggerRepeatModeChangedCallbacks(repeatMode)
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        triggerShuffleModeChangedCallbacks(shuffleModeEnabled)
                    }
                }
                addListener(onPlayerStateChanged)
            }
        }
    }

    private fun handleDurationChanging() {
        launch {
            //give time for player to prepare duration value
            delay(DELAY)
            triggerDurationCallbacks(player!!)
        }
        durationHandled = true
    }

    private fun handlePlayerTracksEnded() {
        triggerPausingCallbacks()
        triggerTracksEndedCallbacks()
        trackEndHandled = true
        isPlaying = false
        player!!.playWhenReady = false
    }

    private fun handlePlayerStopping() {
        if (!stoppingHandled) {
            isStopped = true
            // Not playing because playback ended, the player is buffering, stopped or
            // failed. Check playbackState and player.getPlaybackError for details.
            mediaPreferences.apply {
                setCurrentTrack(currentAudioIndex)
                setCurrentPosition(playbackPosition)
            }
            triggerStoppingCallbacks()
            stoppingHandled = true
        }
    }

    private fun handlePlayerPausing(playWhenReady: Boolean) {
        with(player!!) {
            playbackPosition = currentPosition
            if (playWhenReady != isPlaying) {
                Log.v("registeringAudioSession", " pausing")
                triggerPausingCallbacks()
                isPlaying = playWhenReady
                // Paused by app.
                if (isReleased) isReleased = false

            }
        }

    }

    private fun handlePlayerPlaying(playWhenReady: Boolean) {
        if (playWhenReady != isPlaying) {
            Log.v("registeringAudioSession", " playing  ")
            triggerPlayingCallbacks()
            stoppingHandled = false
            trackEndHandled = false
            isStopped = false
            isPlaying = playWhenReady
            // Active playback.
            //when player start again we start listening to  events of headphone
            if (isReleased) isReleased = false

        }
    }

    private fun handleTrackChanged() {

        with(player!!) {
            durationHandled = false
            stoppingHandled = false
            if (isReleased) isReleased = false
            launch {
                //give time for ui to prepare
                delay(DELAY)
                isPlaying = playWhenReady
                currentAudioIndex = currentWindowIndex
                currentInstance = player.currentTag
                triggerTrackChangedCallbacks()
                Log.v("registeringAudioSession", " tracking  $currentWindowIndex $playWhenReady ")
                //if player was being stopped ,if track changed we trigger the playing callback as an indicator of ending stopping state
                if (isStopped) triggerPlayingCallbacks()
                //trigger onactiveplayer callback at first time
                else if (isFirstTime && playWhenReady) {
                    isFirstTime = false
                    onPlayerStateListListeners.forEach { entry ->
                        entry.value.forEach {
                            it.onActivePlayer()
                        }
                    }
                }

            }
        }
    }

    private fun triggerShuffleModeChangedCallbacks(shuffleModeEnabled: Boolean) {
        onPlayerStateListListeners.forEach {
            it.key.onShuffleModeChanged(shuffleModeEnabled)
        }
    }

    private fun triggerRepeatModeChangedCallbacks(repeatMode: Int) {
        onPlayerStateListListeners.forEach {
            it.key.onRepeatModeChanged(repeatMode)
        }
    }

    private fun triggerDurationCallbacks(player: SimpleExoPlayer) {
        onPlayerStateListListeners.forEach {
            it.key.onDurationChange(player.duration)
        }
    }

    private fun triggerTrackChangedCallbacks() {
        onPlayerStateListListeners.forEach {
            it.key.onAudioChanged(currentAudioIndex, currentInstance)

        }
    }

    private fun triggerTracksEndedCallbacks() {
        onPlayerStateListListeners.forEach {
            it.key.onAudioListCompleted()

        }
    }

    private fun triggerStoppingCallbacks() {
        Log.v("playerstage", "onstop2")
        onPlayerStateListListeners.forEach { entry ->
            entry.key.onStop()
            entry.value.forEach {
                it.onPlayerStop()
            }
        }
    }

    private fun triggerPausingCallbacks() {
        onPlayerStateListListeners.forEach { entry ->
            entry.key.onPause()
            entry.value.forEach {
                it.onInActivePlayer()
            }
        }
    }

    private fun triggerPlayingCallbacks() {
        onPlayerStateListListeners.forEach { entry ->
            entry.key.onPlay()
            entry.value.forEach {
                it.onActivePlayer()
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


}


