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

package com.example.mediaplayer.audioPlayer.audioFocus

import android.media.AudioManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.foregroundService.AudioForegroundService

@Suppress("LeakingThis")
abstract class MediaAudioFocusCompat(service: AudioForegroundService)
    : AudioManager.OnAudioFocusChangeListener, DefaultLifecycleObserver {
    abstract fun requestAudioFocus(audioFocusCallBacks: AudioFocusCallBacks)
    abstract fun abandonAudioFocus()


    init {
        service.lifecycle.addObserver(this)
    }


    override fun onDestroy(owner: LifecycleOwner) {
        abandonAudioFocus()
    }
}

interface AudioFocusCallBacks {
    fun onAudioFocusGained()
    fun onAudioFocusLost(permanent: Boolean)

}
