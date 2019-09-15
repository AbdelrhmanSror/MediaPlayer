/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mediaplayer.foregroundService

import android.content.Context
import com.example.mediaplayer.model.PlayListModel
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class AudioPlayer(private val application: Context) {

    var player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(application)
        private set
    var isPlaying = true
        private set
    var currentAudioIndex = 0
        private set


    companion object {
        fun create(application: Context): AudioPlayer {
            return AudioPlayer(application)
        }
    }

    //creating concatenating media source for media player to play
    private fun buildMediaSource(audioUris: ArrayList<PlayListModel>?): MediaSource? {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(application,
                Util.getUserAgent(application, "MediaPlayer"))
        val concatenatingMediaSource = ConcatenatingMediaSource()
        when (audioUris) {
            null -> return null
            else -> for (item in audioUris) {
                concatenatingMediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(item.audioUri))
            }
        }
        return concatenatingMediaSource
    }

    fun setUpPlayer(audioUris: ArrayList<PlayListModel>, chosenAudioIndex: Int) {
        currentAudioIndex = chosenAudioIndex
        player.apply {
            //to control to player the audio or video right now or wait user to play the audio himself
            playWhenReady = true
            val mediaSource = buildMediaSource(audioUris)
            prepare(mediaSource)
            //to control the starter location of audio
            seekTo(chosenAudioIndex, 0)
        }
    }


    //handle the player when actions happen in notification
    fun setOnPlayerStateChanged(onPlayerStateChanged: OnPlayerStateChanged) {
        player.run {
            addListener(object : Player.EventListener {

                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    //when the track change we update the notification to reflect the current song
                    when {
                        isPlayerNext() || isPlayerPrevious() -> {
                            currentAudioIndex = currentWindowIndex
                            onPlayerStateChanged.onAudioStateChanged()

                        }
                    }
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when {
                        isPlayerStopped() -> {
                            isPlaying = false
                            onPlayerStateChanged.onAudioStateChanged()

                        }
                        isPlayerPlaying() -> {
                            isPlaying = true
                            onPlayerStateChanged.onAudioStateChanged()

                        }

                    }
                }
            })
        }
    }

    fun seekTo(index: Int) {
        currentAudioIndex = index
        player.seekTo(currentAudioIndex, 0)
    }

    fun play() {
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun next() {
        player.next()
    }

    fun previous() {
        when {
            player.currentPosition > 3000 -> player.seekTo(0)
            else -> player.previous()
        }
    }

    fun release() {
        player.release()
    }

    private fun isPlayerStopped(): Boolean {
        player.run {
            return playWhenReady != isPlaying && !playWhenReady
        }
    }

    private fun isPlayerPlaying(): Boolean {
        player.run {
            return playWhenReady != isPlaying && playWhenReady

        }
    }

    private fun isPlayerNext(): Boolean {
        player.run {
            return currentWindowIndex != currentAudioIndex && currentWindowIndex > currentAudioIndex

        }
    }

    private fun isPlayerPrevious(): Boolean {
        player.run {
            return currentWindowIndex != currentAudioIndex && currentWindowIndex < currentAudioIndex

        }
    }

}

interface OnPlayerStateChanged {
    /**
     * whenever the state of audio changed this method will be called on playing , pausing,previous,next
     */
    fun onAudioStateChanged()

}