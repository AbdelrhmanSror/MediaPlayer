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

package com.example.mediaplayer.audioPlayer


interface OnPlayerStateChanged {

    /**
     * this will be called when the player complete playing all the the audio file in the list and no other files to play
     */
    fun onAudioListCompleted() {}

    /**
     * this triggers whenever the audio start  playing
     */
    fun onPlay()

    /**
     * this triggers whenever the audio stop playing
     */
    fun onPause()

    /**
     * this triggers whenever the audio track changes
     * also will trigger when the current audio track changes automatically without interference from user
     */
    fun onAudioChanged(index: Int, isPlaying: Boolean)

    /**
     * this triggers whenever the audio shuffle and repeat mode changes changes
     * also it triggers at initialization time
     */
    fun onShuffleModeChanged(enable: Boolean) {}

    fun onRepeatModeChanged(repeatMode: Int) {}

    fun onDurationChange(duration: Long) {}
    fun onProgressChanged(progress: Long) {}


}