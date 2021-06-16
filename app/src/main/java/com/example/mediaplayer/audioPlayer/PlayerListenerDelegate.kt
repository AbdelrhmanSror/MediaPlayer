/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.audioPlayer

import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.extensions.*
import com.example.mediaplayer.shared.CustomScope
import com.example.mediaplayer.shared.update
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


open class PlayerListenerDelegate(private val service: AudioForegroundService,
                                  private val player: SimpleExoPlayer?
) : CoroutineScope by CustomScope(Dispatchers.Main) {
    private lateinit var onPlayerStateChanged: Player.EventListener

    //list of observer that observer the player it could either be the visible ui application or notification or headset etc..
    private val observers: HashMap<IPlayerObserver, ArrayList<IPlayerListener>> = HashMap()
    private var currentAudioIndex = -1
    var isPlaying = true
        private set
    private var currentInstance: Any? = null
    private var playbackPosition = 0L

    //to prevent the callback of duration to be called more than once at a time
    private var durationHandled: Boolean = false

    //to prevent the callback of track ended to be called more than once at a time
    private var trackEndHandled = false


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
    protected fun setOnPlayerStateChangedListener(observers: HashMap<IPlayerObserver, ArrayList<IPlayerListener>>) {
        this.observers.update(observers)
        player!!.run {
            if (!::onPlayerStateChanged.isInitialized) {
                onPlayerStateChanged = object : Player.EventListener {
                    override fun onPositionDiscontinuity(reason: Int) {
                        if (isTrackChanged(currentAudioIndex, reason))
                            handleTrackChanged()
                    }

                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when {
                            //when the player is ready to be setuped i go on and refelct theses on the ui
                            isNewDurationReady(durationHandled) -> handleDurationChanging()
                            isPlayerPlaying() -> handlePlayerPlaying(playWhenReady)
                            isPlayerPausing() -> handlePlayerPausing(playWhenReady)
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

    //when the user  change the position of seek bar ,it reflects that on text box
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
        noOtherAudiosToPlay()
        trackEndHandled = true
        isPlaying = false
        player!!.playWhenReady = false
    }


    private fun handlePlayerPausing(playWhenReady: Boolean) {
        with(player!!) {
            playbackPosition = currentPosition
            if (playWhenReady != isPlaying) {
                triggerPausingCallbacks()
                isPlaying = playWhenReady
                // Paused by app.
                if (isReleased) isReleased = false

            }
        }

    }

    private fun handlePlayerPlaying(playWhenReady: Boolean) {
        if (playWhenReady != isPlaying) {
            triggerPlayingCallbacks()
            trackEndHandled = false
            isPlaying = playWhenReady
            // Active playback.
            //when player start again we start listening to  events of headphone
            if (isReleased) isReleased = false

        }
    }

    private fun handleTrackChanged() {

        with(player!!) {
            durationHandled = false
            if (isReleased) isReleased = false
            launch {
                //give time for ui to prepare
                delay(DELAY)
                isPlaying = playWhenReady
                currentAudioIndex = currentWindowIndex
                currentInstance = player.currentTag
                triggerTrackChangedCallbacks()


            }
        }
    }

    private fun triggerShuffleModeChangedCallbacks(shuffleModeEnabled: Boolean) {
        observers.forEach {
            it.key.onShuffleModeChanged(shuffleModeEnabled)
        }
    }

    private fun triggerRepeatModeChangedCallbacks(repeatMode: Int) {
        observers.forEach {
            it.key.onRepeatModeChanged(repeatMode)
        }
    }

    private fun triggerDurationCallbacks(player: SimpleExoPlayer) {
        observers.forEach {
            it.key.onDurationChange(player.duration)
        }
    }

    private fun triggerTrackChangedCallbacks() {
        observers.forEach {
            it.key.onAudioChanged(currentAudioIndex, currentInstance)

        }
    }

    private fun noOtherAudiosToPlay() {
        observers.forEach {
            it.key.onAudioListCompleted()

        }
    }

    private fun triggerPausingCallbacks() {
        observers.forEach { entry ->
            entry.key.onPause()
            entry.value.forEach {
                it.onInActivePlayer()
            }
        }
    }

    private fun triggerPlayingCallbacks() {
        observers.forEach { entry ->
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

    protected fun setAudioSessionChangeListener(updatedPlayerObserver: IPlayerObserver): IPlayerListener {
        return OnAudioSessionIdChangeListener.createOrUpdate(service, player!!, updatedPlayerObserver)

    }

    protected fun setOnProgressChangedListener(iPlayerObserver: IPlayerObserver): IPlayerListener {
        val onProgressChanged = OnAudioProgressChangeListener(player!!)
        iPlayerObserver.onProgressChangedLiveData(onProgressChanged)
        return onProgressChanged

    }


}


