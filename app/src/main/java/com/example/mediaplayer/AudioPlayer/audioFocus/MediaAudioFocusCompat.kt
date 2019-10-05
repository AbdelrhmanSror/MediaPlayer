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
import android.media.AudioManager
import android.os.Build

abstract class MediaAudioFocusCompat : AudioManager.OnAudioFocusChangeListener {
    abstract fun requestAudioFocus(audioFocusCallBacks: AudioFocusCallBacks)

}

interface AudioFocusCallBacks {
    fun onAudioFocusGained()
    fun onAudioFocusLost(Permanent: Boolean)

}

object MediaAudioFocusCompatFactory {
    fun create(context: Context): MediaAudioFocusCompat {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> MediaAudioFocus.create(context)
            else -> MediaAudioFocusPre.create(context)
        }

    }
}