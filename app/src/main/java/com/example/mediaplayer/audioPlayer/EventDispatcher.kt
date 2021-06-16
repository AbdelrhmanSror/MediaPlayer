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

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.KeyEvent

class EventDispatcher constructor(
        context: Context
) {

    companion object {
        @JvmStatic
        private val TAG = "SM:${EventDispatcher::class.java.simpleName}"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    enum class Event {
        PLAY_PAUSE,
        PLAY,
        PAUSE,
        STOP,
        SKIP_NEXT,
        SKIP_PREVIOUS,
    }

    fun dispatchEvent(event: Event) {
        Log.v(TAG, "dispatchEvent $event")

        val keycode = when (event) {
            Event.PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            Event.PLAY -> KeyEvent.KEYCODE_MEDIA_PLAY
            Event.PAUSE -> KeyEvent.KEYCODE_MEDIA_PAUSE
            Event.STOP -> KeyEvent.KEYCODE_MEDIA_STOP
            Event.SKIP_NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
            Event.SKIP_PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS

        }
        dispatchMediaKeyEvent(keycode)
    }

    fun dispatchMediaKeyEvent(keyCode: Int) {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}