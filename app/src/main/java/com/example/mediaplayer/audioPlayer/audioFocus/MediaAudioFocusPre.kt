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

import android.content.Context
import android.media.AudioManager

/**
 * worked on devices pre api 26 (oreo)
 */
@Suppress("DEPRECATION")
class MediaAudioFocusPre constructor(context: Context) : MediaAudioFocusCompat {

    private var audioFocusCallBacks: AudioFocusCallBacks? = null

    private var audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var isFocusLost = true
    private var result = audioManager.requestAudioFocus(this,
            // Use the music stream.
            AudioManager.STREAM_MUSIC,
            // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN)


    override fun requestAudioFocus(audioFocusCallBacks: AudioFocusCallBacks) {
        if (isFocusLost) {
            this.audioFocusCallBacks = audioFocusCallBacks
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                isFocusLost = false
                audioFocusCallBacks.onAudioFocusGained()
            } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                isFocusLost = true
                audioFocusCallBacks.onAudioFocusLost(true)
            }
        }
    }


    override fun abandonAudioFocus() {
        isFocusLost = true
        audioManager.abandonAudioFocus(this)

    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                isFocusLost = true
                audioFocusCallBacks?.onAudioFocusLost(true)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                isFocusLost = false

                audioFocusCallBacks?.onAudioFocusLost(false)

            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                isFocusLost = false

                // Your app has been granted audio focus again
                audioFocusCallBacks?.onAudioFocusGained()

            }
        }
    }

}