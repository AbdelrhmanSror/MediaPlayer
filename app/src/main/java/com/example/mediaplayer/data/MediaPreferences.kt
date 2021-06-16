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

package com.example.mediaplayer.data

import android.content.Context
import com.example.mediaplayer.extensions.getItem
import com.example.mediaplayer.extensions.getSharedPrefrence
import com.example.mediaplayer.extensions.putItem


private const val TAG = "MediaPreferences"
private const val CURRENT_WINDOW = "${TAG}.current window"
private const val PLAYBACK_POSITION = "${TAG}.playback position"

class MediaPreferences constructor(context: Context) {
    private val preferences = context.getSharedPrefrence(TAG)
    fun getCurrentTrack(): Int {
        return preferences!!.getItem(CURRENT_WINDOW, 0)
    }

    fun getCurrentPosition(): Long {
        return preferences!!.getItem(PLAYBACK_POSITION, 0)
    }

    fun setCurrentTrack(id: Int) {
        return preferences!!.putItem(CURRENT_WINDOW, id)
    }

    fun setCurrentPosition(position: Long) {
        return preferences!!.putItem(PLAYBACK_POSITION, position)
    }
}