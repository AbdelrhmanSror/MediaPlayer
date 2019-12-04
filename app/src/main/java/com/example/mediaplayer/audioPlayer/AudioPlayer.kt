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

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.audioPlayer.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompatFactory
import com.example.mediaplayer.foregroundService.NoiseBroadCastReceiver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class AudioPlayer(private val context: Context) : LifecycleObserver, AudioPlayerObservable {

    private var player: SimpleExoPlayer? = ExoPlayerFactory.newSimpleInstance(context)

    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(context, context.packageName)
    }

    //to give flexibility if i want to do extra work while releasing the player like stopping service
    private lateinit var extraRelease: () -> Unit

    private val mediaSessionConnector: MediaSessionConnector by lazy {
        MediaSessionConnector(mediaSession)
    }
    private var audioSessionId: Int = -1
    //to see if the ui is visible or not
    private var isUiVisible = true

    /**
     * //to indicate if the player is released or not so when the ui is not visible we release the player
     * this is to avoid reinitializing the player again when user release the player and ui
     * is still visible so if he resume the player we do not have to initialize it again
     */
    private var isReleased = false

    var isPlaying = true
        private set

    var currentAudioIndex = -1
        private set
    var playerShuffleMode = player!!.shuffleModeEnabled
        private set
    var playerRepeatMode = player!!.repeatMode
        private set
    var playerDuration = 0L
        private set


    private val onPlayerStateChanged = ArrayList<OnPlayerStateChanged?>()

    private lateinit var runnable: Runnable

    private var handler: Handler? = null

    /**
     * intent filter to setup with broadcast receiver so when user disconnect the headphone we pause the player
     */
    private val intentFilter = IntentFilter().apply {
        addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }
    private val myNoisyAudioStreamReceiver = NoiseBroadCastReceiver()


    private var repeatModeActivated: Boolean = false
        set(value) {
            if (value) {
                player!!.repeatMode = Player.REPEAT_MODE_ALL
            } else {
                player!!.repeatMode = Player.REPEAT_MODE_OFF

            }
            field = value
        }


    private var shuffleModeActivated: Boolean = false
        set(value) {
            player!!.shuffleModeEnabled = value
            field = value
        }
    private var songList: ArrayList<Uri>? = null

    private val mMediaAudioFocus = MediaAudioFocusCompatFactory.create(context)

    /**
     * variable to indicate to the last state of player if audio focus happened
     * so if the last state of player was true then continue playing the audio after the focus gained otherwise do nothing
     * because user himself paused the player so it makes no sense to continue playing as it was already paused
     */
    private var prevPlayerState = false

    //var indicates if the focus is permanently lost so we can request focus again
    private var isFocusPermanentLost = false

    companion object {
        fun create(application: Context): AudioPlayer {
            return AudioPlayer(application)
        }
    }

    //creating concatenating media source for media player to play_notification
    private fun buildMediaSource(audioUris: ArrayList<Uri>?): MediaSource? {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "MediaPlayer"))
        val concatenatingMediaSource = ConcatenatingMediaSource()
        when (audioUris) {
            null -> return null
            else -> for (item in audioUris) {
                concatenatingMediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(item))
            }
        }
        return concatenatingMediaSource
    }

    private fun setUpPlayer(chosenAudioIndex: Int) {
        currentAudioIndex = chosenAudioIndex
        player?.apply {
            //to control to player the audio or video right now or wait user to play_collapsed_notification the audio himself
            playWhenReady = true
            val mediaSource = buildMediaSource(songList)
            prepare(mediaSource)
            //to control the starter location of audio and current track
            seekTo(chosenAudioIndex)
            context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)

        }
    }


    fun startPlayer(audioList: ArrayList<Uri>, chosenAudioIndex: Int) {
        //only re setup the player when the playlist changes
        if (audioList != songList) {
            songList = audioList
            setUpPlayer(chosenAudioIndex)
            /* //trigger these callback for first time every time player is being started because these call back won't trigger so we do it manually
             onPlayerStateChanged.forEach {
                 it?.onRepeatModeChanged(player!!.repeatMode)
                 it?.onShuffleModeChanged(player!!.shuffleModeEnabled)
             }*/
        } else {
            seekTo(chosenAudioIndex)
            //if the player was being stopped then play
            if (!isPlaying) {
                Handler().postDelayed({
                    play()
                }, 1000)

            }
        }
        requestFocus()

    }

    /**
     * to control the player through headset or google assistant
     */
    fun enableCommandControl(mediaDescriptionCompat: (Int) -> MediaDescriptionCompat) {
        mediaSessionConnector.setPlayer(player)
        mediaSession.isActive = true
        val queueNavigator: TimelineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
                return mediaDescriptionCompat(windowIndex)
            }

        }
        mediaSessionConnector.setQueueNavigator(queueNavigator)
    }


    init {
        setOnPlayerStateChanged()
    }

    /**
     * to trigger call backs instantly once the the observer is attached
     *
     *[instantTrigger] set this if u enter ur activity or fragment from notification so u want to trigger callback to refresh ui.
     * if u set [instantTrigger] and player was not already playing it won't have any effect,it is only active if the player was playing
     *
     *this function will trigger these callbacks if possible
     * onPlayerStateChanged.onAudioChanged)
     * onPlayerStateChanged.onShuffleModeChanged
     * onPlayerStateChanged.onRepeatModeChanged
     * onPlayerStateChanged.onDurationChange
     *
     * warning :do not use [instantTrigger] other than that because it may trigger callback twice at same time which will lead to unwanted behaviour
     */
    fun instantTrigger(onPlayerStateChanged: OnPlayerStateChanged) {
        /**
         *audio player at initial state will return -1 because there is no audio is being played
         * also we just want to trigger these manually to refresh ui cause
         * if the audio is playing these events won't trigger until the events that trigger them happen
         * so to refresh ui when activity or fragment rebind with the service we have to do it manually
         * also we do it only if audio already playing otherwise we continue the normal flow
         */
        if (currentAudioIndex != -1) {
            notifyObserver(onPlayerStateChanged)

        }
    }

    /**
     *to enable progress callback of audio ,can be used in ui to setup timer
     */
    fun enableProgress(onPlayerStateChanged: OnPlayerStateChanged) {
        setOnProgressChanged(onPlayerStateChanged)
    }

    /**
     *to trigger auio session call back first time observer is attached ,it won't trigger the callback if the audioSession is -1
     */
    fun triggerAudioSessionCallbackFirstTime(onPlayerStateChanged: OnPlayerStateChanged) {
        if (audioSessionId == -1)
            setOnAudioSessionIdChangeListener()
        else onPlayerStateChanged.onAudioSessionId(audioSessionId)


    }


    /**
     * to register observer we need to give it the class that implement the interface of observer
     */
    override fun registerObserver(onPlayerStateChanged: OnPlayerStateChanged) {
        this.onPlayerStateChanged.add(onPlayerStateChanged)
    }

    override fun removeObserver(onPlayerStateChanged: OnPlayerStateChanged) {
        stopProgress()
        this.onPlayerStateChanged.remove(onPlayerStateChanged)
    }

    override fun notifyObserver(onPlayerStateChanged: OnPlayerStateChanged) {
        onPlayerStateChanged.onAudioChanged(currentAudioIndex, isPlaying)
        onPlayerStateChanged.onShuffleModeChanged(playerShuffleMode)
        onPlayerStateChanged.onRepeatModeChanged(playerRepeatMode)
        onPlayerStateChanged.onDurationChange(playerDuration)


    }


    //handle the player when actions happen in notification
    private fun setOnPlayerStateChanged() {
        player!!.run {
            addListener(object : Player.EventListener {
                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    //when the track changed we update the index of song reflect the current song
                    when {
                        isPlayerNext() || isPlayerPrevious() -> {
                            currentAudioIndex = currentWindowIndex
                            onPlayerStateChanged.forEach {
                                it?.onAudioChanged(currentAudioIndex, isPlaying)
                            }
                            setDuration()


                        }

                    }
                }


                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when {
                        isPlayerStopped() -> {
                            //when player stop we stop listening to plug off events of headphone because player is already stopped
                            context.unregisterReceiver(myNoisyAudioStreamReceiver)
                            isPlaying = false
                            onPlayerStateChanged.forEach {
                                it?.onPause()
                            }

                        }
                        isPlayerPlaying() -> {
                            if (isFocusPermanentLost)
                                requestFocus()
                            //when player start again we start listening to  events of headphone
                            context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                            isPlaying = true
                            onPlayerStateChanged.forEach {
                                it?.onPlay()
                            }


                        }
                        playbackState == ExoPlayer.STATE_ENDED -> {
                            onPlayerStateChanged.forEach {
                                it?.onAudioListCompleted()
                            }

                        }


                    }
                }


                override fun onRepeatModeChanged(repeatMode: Int) {
                    playerRepeatMode = repeatMode
                    onPlayerStateChanged.forEach {
                        it?.onRepeatModeChanged(repeatMode)
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    playerShuffleMode = shuffleModeEnabled
                    onPlayerStateChanged.forEach {
                        it?.onShuffleModeChanged(shuffleModeEnabled)
                    }
                }
            })
        }
    }

    private fun setOnAudioSessionIdChangeListener() {
        player?.addAnalyticsListener(object : AnalyticsListener {

            /**
             * Called when the audio session id is set.
             *
             * @param eventTime      The event time.
             * @param audioSessionId The audio session id.
             */
            override fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {
                Log.v("visulaizerBytes", " audio seesioon1 $audioSessionId")
                this@AudioPlayer.audioSessionId = audioSessionId
                onPlayerStateChanged.forEach {
                    it?.onAudioSessionId(audioSessionId)
                }

            }
        })

    }

    /**
     * request focus for audio player to start
     */
    private fun requestFocus() {
        mMediaAudioFocus.requestAudioFocus(object : AudioFocusCallBacks {
            //when the focus gained we start playing audio if it was previously running
            override fun onAudioFocusGained() {
                if (prevPlayerState) {
                    play()
                }

            }

            //when the focus lost we pause the player and set prevPlayerState to the current state of player
            override fun onAudioFocusLost(Permanent: Boolean) {
                prevPlayerState = isPlaying
                pause()
                isFocusPermanentLost = Permanent
            }
        })
    }

    /**
     * getting the duration of current playing audio
     * to get duration it require player to be in ready state so we handle
     * this in runnable so to keep trying until getting the duration
     */
    private fun setDuration() {
        val handler = Handler()
        var runnable: Runnable? = null
        runnable = Runnable {
            if (player?.playbackState == ExoPlayer.STATE_READY) {
                playerDuration = player?.duration!!
                onPlayerStateChanged.forEach {
                    it?.onDurationChange(player?.duration!!)
                }
            } else
                handler.postDelayed(runnable!!, 0)
        }
        handler.post(runnable)
        handler.postDelayed(runnable, 0)

    }

    /**
     * enable repeat mode
     */
    fun repeatModeEnable() {
        repeatModeActivated = !repeatModeActivated

    }

    /**
     * enable shuffle mode
     */
    fun shuffleModeEnable() {
        shuffleModeActivated = !shuffleModeActivated

    }

    /**
     * seek to different track
     */
    fun seekTo(index: Int) {
        currentAudioIndex = index
        player?.seekTo(currentAudioIndex, 0)
        onPlayerStateChanged.forEach {
            it?.onAudioChanged(currentAudioIndex, isPlaying)
        }
        if (!isPlaying)
            play()
        setDuration()

    }

    /**
     * seek to different position
     */
    fun seekToSecond(second: Int) {
        player?.seekTo(second * 1000.toLong())
    }

    /**
     * play audio and reset runnable callback of Audio progress if it was initialized before
     */
    fun play() {
        isReleased = false
        resumeProgress()
        player?.playWhenReady = true
    }

    /**
     * pause audio and remove runnable callback of Audio progress if it is initialized
     */
    fun pause() {
        isReleased = false
        pauseProgress()
        player?.playWhenReady = false
    }

    /**
     * go to next audio
     */
    fun next() {
        isReleased = false
        player?.next()
    }

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    fun previous() {
        isReleased = false
        player?.apply {
            when {
                currentPosition > 3000 -> seekTo(0)
                else -> previous()
            }
        }
    }

    /**
     * change the audio state from playing to pausing and vice verse
     */
    fun changeAudioState() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    /**
     * to release audio player when lifecycle owner is destroyed
     */
    fun release(extra: () -> Unit) {
        extraRelease = extra
        player?.let {
            pause()
            if (!isUiVisible) {
                releasePlayerPermanently()
            } else {
                isReleased = true
            }
        }
    }

    private fun releasePlayerPermanently() {
        extraRelease()
        player!!.release()
        player = null
        mediaSession.release()
        mediaSessionConnector.setPlayer(null)
    }

    /**
     *detect if player already stopped
     * if the current mode of player does not equal the last mode then the player mode has changed
     * the second condition detect if player mode has changed to mode playing or stopped
     */

    private fun isPlayerStopped(): Boolean {
        player!!.run {
            return playWhenReady != isPlaying && !playWhenReady
        }
    }


    /**
     * detect if player already playing
     */
    private fun isPlayerPlaying(): Boolean {
        player!!.run {
            return playWhenReady != isPlaying && playWhenReady

        }
    }

    /**
     * detect if player already started to play next audio
     * if the current mode of player does not equal the last mode then the player mode has changed
     * the second condition detect if player mode has changed to next audio or previous audio
     */
    private fun isPlayerNext(): Boolean {
        player!!.run {
            return currentWindowIndex != currentAudioIndex && currentWindowIndex > currentAudioIndex

        }
    }
    //detect if player already started to play previous audio

    private fun isPlayerPrevious(): Boolean {
        player!!.run {
            return currentWindowIndex != currentAudioIndex && currentWindowIndex < currentAudioIndex

        }
    }

    private fun setOnProgressChanged(onPlayerStateChanged: OnPlayerStateChanged) {
        onPlayerStateChanged.onProgressChangedLiveData(ProgressLiveData())
    }

    inner class ProgressLiveData : MutableLiveData<Long>() {
        init {
            handler = Handler()
            runnable = Runnable {
                player?.let {
                    // onPlayerStateChanged.onProgressChanged(player!!.currentPosition)
                    postValue(player!!.currentPosition)
                    //update the text position under seek bar to reflect the current position of seek bar
                    handler?.postDelayed(runnable, 50)
                }
            }
            handler?.postDelayed(runnable, 50)
        }

        override fun onInactive() {
            pauseProgress()

        }

        override fun onActive() {
            isUiVisible = true
            resumeProgress()
        }

    }

    private fun pauseProgress() {
        if (::runnable.isInitialized) {
            handler?.removeCallbacks(runnable)
        }
    }

    private fun resumeProgress() {
        if (::runnable.isInitialized) {
            handler?.post(runnable)
        }
    }

    private fun stopProgress() {
        isUiVisible = false
        pauseProgress()
        if (isReleased) {
            releasePlayerPermanently()
        }
        handler = null
    }

}
