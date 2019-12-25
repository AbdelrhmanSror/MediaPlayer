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

interface IPlayerObservable {
    fun registerObserver(iPlayerObserver: IPlayerObserver, audioSessionIdCallbackEnable: Boolean = false
                         , audioNoisyControlEnable: Boolean = true
                         , progressCallBackEnabled: Boolean = false,
                         isMainObserver: Boolean = false)

    fun registerObservers(vararg iPlayerObserver: IPlayerObserver)

    fun removeAllObservers()
    fun getCountOfMainObservers(): Int
    fun removeObserver(iPlayerObserver: IPlayerObserver)
    fun notifyObserver(iPlayerObserver: IPlayerObserver)
    /**
     * will be called to check if it is appropriate to release player now
     * the best place to call this after [removeAllObservers] if u want to release the player immediately
     */
    fun invalidate()
}