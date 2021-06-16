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

package com.example.mediaplayer.audioPlayer.audioFocus

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.AudioPlayerModel
import com.example.mediaplayer.audioPlayer.IPlayerObserver
import com.example.mediaplayer.shared.CustomScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FocusRequestImp constructor(private val mediaAudioFocusCompat: MediaAudioFocusCompat, service: AudioForegroundService, private val player: AudioPlayer) : IPlayerObserver,
        CoroutineScope by CustomScope(Dispatchers.Main), DefaultLifecycleObserver {

    private var isFocusLostAgain = false


    override fun onDestroy(owner: LifecycleOwner) {
        mediaAudioFocusCompat.abandonAudioFocus()
    }

    init {
        service.lifecycle.addObserver(this)
    }

    /**
     * request focus for audio player to start
     */
    private fun requestFocus() {
        mediaAudioFocusCompat.requestAudioFocus(object : AudioFocusCallBacks {
            override fun onAudioFocusGained() {
                isFocusLostAgain = false
                launch {
                    delay(2000)
                    if (!isFocusLostAgain) {
                        //if the action was from audiofocus we continue playing otherwise if it was pause we abandon the focus
                        if (player.actionFromAudioFocus) {
                            player.play(true)
                        } else if (!player.isPlaying) {
                            mediaAudioFocusCompat.abandonAudioFocus()
                        }
                    }
                }


            }

            //when the focus lost we pause the player and set prevPlayerState to the current state of player
            override fun onAudioFocusLost(permanent: Boolean) {
                isFocusLostAgain = true
                player.pause(!permanent)
            }
        })

    }


    override fun onAttached(audioPlayerModel: AudioPlayerModel) {
        requestFocus()
    }

    override fun onPlay() {
        requestFocus()
    }

    override fun onPause() {
        if (!player.actionFromAudioFocus) {
            mediaAudioFocusCompat.abandonAudioFocus()
        }
    }

}
