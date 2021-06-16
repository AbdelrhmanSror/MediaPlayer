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

import android.util.Log
import kotlinx.coroutines.*


class MediaButton constructor(
        private val eventDispatcher: EventDispatcher

) : CoroutineScope by MainScope() {

    companion object {
        @JvmStatic
        private val TAG = "SM:${MediaButton::class.java.simpleName}"
        internal const val DELAY = 300L
        internal const val MAX_ALLOWED_CLICKS = 3
    }

    private var clicks = 0

    private var job: Job? = null

    fun onHeatSetHookClick() {
        Log.v(TAG, "onHeatSetHookClick")
        clicks++

        if (clicks <= MAX_ALLOWED_CLICKS) {
            job?.cancel()
            job = launch {
                delay(DELAY)
                dispatchEvent(clicks)
                clicks = 0
            }
        }
    }

    private fun dispatchEvent(clicks: Int) {
        Log.v(TAG, "dispatchEvent clicks=$clicks")

        when (clicks) {
            0 -> {
            }
            1 -> eventDispatcher.dispatchEvent(EventDispatcher.Event.PLAY_PAUSE)
            2 -> eventDispatcher.dispatchEvent(EventDispatcher.Event.SKIP_NEXT)
            3 -> eventDispatcher.dispatchEvent(EventDispatcher.Event.SKIP_PREVIOUS)
        }
    }

}
