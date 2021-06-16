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

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

class MediaSessionConnectorAdapter constructor(private val mediaSessionCompat: MediaSessionCompat,
                                               private val mediaSessionConnector: MediaSessionConnector) {
    private lateinit var audioPlayer: AudioPlayer

    /**
     * Default { @link ControlDispatcher } that dispatches all operations to the player without
     * modification.
     */
    inner class CustomControlDispatcher : ControlDispatcher {

        override fun dispatchSetPlayWhenReady(player: Player, playWhenReady: Boolean): Boolean {
            if (playWhenReady) audioPlayer.play()
            else audioPlayer.pause()
            return false
        }

        override fun dispatchSeekTo(player: Player, windowIndex: Int, positionMs: Long): Boolean {
            player.seekTo(windowIndex, positionMs)
            return true
        }

        override fun dispatchSetRepeatMode(player: Player, repeatMode: Int): Boolean {
            player.repeatMode = repeatMode
            return true
        }

        override fun dispatchSetShuffleModeEnabled(player: Player, shuffleModeEnabled: Boolean): Boolean {
            player.shuffleModeEnabled = shuffleModeEnabled
            return true
        }

        override fun dispatchStop(player: Player, reset: Boolean): Boolean {
            audioPlayer.pause()
            audioPlayer.releaseIfPossible()
            return false
        }
    }

    fun release() {
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
    }

    fun setPlayers(audioPlayer: AudioPlayer, player: SimpleExoPlayer) {
        this.audioPlayer = audioPlayer
        mediaSessionConnector.setPlayer(player)
        mediaSessionConnector.setControlDispatcher(CustomControlDispatcher())
        mediaSessionCompat.isActive = true
    }

    fun setQueueNavigator(mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        val queueNavigator: TimelineQueueNavigator = object : TimelineQueueNavigator(mediaSessionCompat) {
            override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
                return mediaDescriptionCompat(windowIndex)
            }

        }
        mediaSessionConnector.setQueueNavigator(queueNavigator)
    }
}

