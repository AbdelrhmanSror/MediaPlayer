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

package com.example.mediaplayer.AudioPlayer.audioFocus

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 *worked on devices with api 26 (oreo) and later

 */
@RequiresApi(api = Build.VERSION_CODES.O)
class MediaAudioFocus private constructor(context: Context) : MediaAudioFocusCompat() {

    private val focusLock = Any()
    private lateinit var audioFocusCallBacks: AudioFocusCallBacks
    private var playbackDelayed = false
    private var playbackNowAuthorized = false
    private var resumeOnFocusGain = false


    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setAcceptsDelayedFocusGain(true)
        //setWillPauseWhenDucked(true)
        setOnAudioFocusChangeListener(this@MediaAudioFocus)
        build()
    }

    companion object {
        fun create(context: Context): MediaAudioFocus {
            return MediaAudioFocus(context)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {

                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    audioFocusCallBacks.onAudioFocusGained()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                audioFocusCallBacks.onAudioFocusLost(true)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
                audioFocusCallBacks.onAudioFocusLost(false)
            }

        }


    }

    override fun requestAudioFocus(audioFocusCallBacks: AudioFocusCallBacks) {
        this.audioFocusCallBacks = audioFocusCallBacks
        val res = audioManager.requestAudioFocus(focusRequest)
        synchronized(focusLock) {
            playbackNowAuthorized = when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                    false
                }
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    audioFocusCallBacks.onAudioFocusGained()
                    true
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    false
                }
                else -> false
            }
        }


    }


}

