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

import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.data.MediaPreferences
import com.google.android.exoplayer2.SimpleExoPlayer


class AudioPlayer constructor(private val service: AudioForegroundService,
                              private val mediaSessionConnectorAdapter: MediaSessionConnectorAdapter,
                              private var player: SimpleExoPlayer?,
                              private val mediaPreferences: MediaPreferences)
    : PlayerListenerDelegate(service, player!!),
        DefaultLifecycleObserver,
        IPlayerControl by PlayerControlDelegate(service, player),
        IPlayerObservable {


    /**
     * store observers and their corresponding listeners into hash map so it could be easily to notify or remove listener when registered observer
     */
    private val observers: HashMap<IPlayerObserver, ArrayList<IPlayerListener>> = HashMap()


    private val mainObservers: HashSet<IPlayerObserver> = HashSet()

    private var isNoisyModeEnabled = false

    /**
     * to give flexibility if i want to do extra work while releasing the player
     * if u want do any thing from service while releasing the player u could set  [extraRelease]
     * so when player has got released this [extraRelease] will be called
     */
    @Suppress
    var extraRelease: (() -> Unit)? = null


    init {
        service.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.v("playerstage", "ondestoy")
        releasePlayerPermanently()

    }

    override fun registerObservers(vararg iPlayerObserver: IPlayerObserver) {
        iPlayerObserver.forEach {
            registerObserver(it)
        }
    }

    /**
     * to register observer we need to give it the class that implement the interface of observer
     */
    override fun registerObserver(iPlayerObserver: IPlayerObserver, audioSessionIdCallbackEnable: Boolean
                                  , audioNoisyControlEnable: Boolean
                                  , progressCallBackEnabled: Boolean
                                  , isMainObserver: Boolean) {
        /**
         * as long as this observer is still registered as observer the app can not be released
         */
        if (isMainObserver)
            mainObservers.add(iPlayerObserver)
        if (!observers.containsKey(iPlayerObserver)) {
            //only setup noisy filter once
            if (noisyIsNotEnabled(audioNoisyControlEnable)) {
                isNoisyModeEnabled = true
                observers[iPlayerObserver] = getListOfListeners(audioSessionIdCallbackEnable, iPlayerObserver, true, progressCallBackEnabled)
            } else
                observers[iPlayerObserver] = getListOfListeners(audioSessionIdCallbackEnable, iPlayerObserver, false, progressCallBackEnabled)
            notifyObserver(iPlayerObserver)
            setOnPlayerStateChangedListener(observers)
        }
        Log.v("observers", "$observers")
    }

    private fun noisyIsNotEnabled(audioNoisyControlEnable: Boolean) =
            !isNoisyModeEnabled && audioNoisyControlEnable

    /**
     * get list of listeners that is registered to be triggered
     */
    private fun getListOfListeners(audioSessionIdCallbackEnable: Boolean,
                                   iPlayerObserver: IPlayerObserver, audioNoisyControlEnable: Boolean,
                                   progressCallBackEnabled: Boolean): ArrayList<IPlayerListener> {
        val listOfListeners = arrayListOf<IPlayerListener>()
        if (audioSessionIdCallbackEnable) listOfListeners.add(setAudioSessionChangeListener(iPlayerObserver))
        if (audioNoisyControlEnable) listOfListeners.add(setNoisyListener())
        if (progressCallBackEnabled) listOfListeners.add(setOnProgressChangedListener(iPlayerObserver))
        return listOfListeners
    }

    /**
     *
     * call this if u just want to un register your observer,this will not release the player
     *
     * if u want to release every thing use [releaseIfPossible]
     *
     * if you have registered to listen to progress this [removeObserver] will stop the progress
     *
     * NOTE: this will act as [releaseIfPossible] if there is only one observer so no need to call both together just one of them
     */
    override fun removeObserver(iPlayerObserver: IPlayerObserver) {
        //calling onDatch fun of every listener first
        observers[iPlayerObserver]?.forEach {
            it.onObserverDetach(iPlayerObserver)
        }
        //removing the observer
        mainObservers.remove(iPlayerObserver)
        observers.remove(iPlayerObserver)
        invalidate()
        //update the observer in the playerStateChangeListener so if any event happened it will have the latest list of observers
        setOnPlayerStateChangedListener(observers)
    }

    private fun triggerOnDetachCallbacks() {
        //iterating over all observer and call ondetach method
        observers.keys.forEach { observer ->
            observers[observer]?.forEach { listener ->
                listener.onObserverDetach(observer)
            }
        }
    }

    override fun removeAllObservers() {
        triggerOnDetachCallbacks()
        observers.clear()
    }


    override fun notifyObserver(iPlayerObserver: IPlayerObserver) {
        with(player!!) {
            iPlayerObserver.onAttached(AudioPlayerModel(
                    currentIndex(),
                    isPlaying,
                    shuffleModeEnabled,
                    repeatMode,
                    currentTag()))
        }


    }


    /**
     * to control the player through headset or google assistant
     */
    fun setCommandControl(mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        mediaSessionConnectorAdapter.setPlayers(this, player!!)
        mediaSessionConnectorAdapter.setQueueNavigator(mediaDescriptionCompat)
    }

    override fun getCountOfMainObservers(): Int {
        return mainObservers.size
    }

    /**
     *will decide if its appropriate to release the player or not
     */
    override fun invalidate() {
        if (isReleased && getCountOfMainObservers() == 0 && !isPlaying) {
            service.stopSelf()

        }
    }

    /**
     * call this if u want to release the player and every thing
     * without taking in consideration the live main observers and current state of player
     *
     */
    private fun releasePlayerPermanently() {
        removeAllObservers()
        mediaSessionConnectorAdapter.release()
        extraRelease?.invoke()
        player?.release()
        player = null
    }


    /**
     * if u want to release every thing use [releaseIfPossible]
     *
     * this has no effect if number of main observer larger than 1
     *
     * this will delay the releasing of player until the main observers are not attached unless user resume the player again then nothing wil happen
     *
     * this best for avoiding releasing player when ui is visible also to avoid preparing player again after that
     *
     * if u want to release player immediately call [removeAllObservers] or just remove the main observers then [pause] then [releaseIfPossible]
     */
    fun releaseIfPossible() {
        player.let {
            isReleased = true
            invalidate()

        }
    }

}