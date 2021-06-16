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

import android.net.Uri

interface IPlayerControl {

    var actionFromAudioFocus: Boolean
    /**
     * return current player index
     */
    fun currentIndex(): Int

    /**
     * return current player position in milliseconds
     */
    fun currentPosition(): Long

    /**
     * return current instance that refer to current playing audio
     * that is passed when calling [setUpPlayer] if nothing passed will return null
     */
    fun currentTag(): Any?


    fun setUpPlayer(audioList: List<Any>? = emptyList(), audioUris: List<Uri>, index: Int)

    /**
     * enable repeat mode
     */
    fun repeatModeEnable()

    /**
     * enable shuffle mode
     */
    fun shuffleModeEnable()

    /**
     * seek to different track
     */
    fun seekToIndex(index: Int)

    /**
     * seek to different position
     */
    fun seekToSecond(second: Int)

    /**
     * play audio and reset runnable callback of Audio progress if it was initialized before
     */
    fun play(fromAudioFocus: Boolean = false)

    /**
     * pause audio and remove runnable callback of Audio progress if it is initialized
     */
    fun pause(fromAudioFocus: Boolean = false)

    /**
     * go to next audio
     */
    fun next()

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    fun previous()

    /**
     * change the audio state from playing to pausing and vice verse
     *
     * to change the current state always use this method, if u tried to use play or pause method will cause unwanted behaviour
     */
    fun changeAudioState()

    /**
     * swaping two items in media source
     */
    fun swapItems(first: Int, second: Int)

}