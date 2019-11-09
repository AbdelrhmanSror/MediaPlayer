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
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.mediaplayer.audioPlayer.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompatFactory
import com.example.mediaplayer.foregroundService.AudioBroadCastReceiver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class AudioPlayer(private val application: Context, lifecycleOwner: LifecycleOwner) : LifecycleObserver {

    private var player: SimpleExoPlayer? = ExoPlayerFactory.newSimpleInstance(application)

    var isPlaying = true
        private set

    var currentAudioIndex = 0
        private set

    private var onPlayerStateChanged: OnPlayerStateChanged? = null

    private lateinit var runnable: Runnable

    private lateinit var handler: Handler

    /**
     * intent filter to setup with broadcast receiver so when user disconnect the headphone we pause the player
     */
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val myNoisyAudioStreamReceiver = AudioBroadCastReceiver()
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
    private var mSongList: ArrayList<Uri>? = null

    private val mMediaAudioFocus = MediaAudioFocusCompatFactory.create(application)

    /**
     * variable to indicate to the last state of player if audio focus happened
     * so if the last state of player was true then continue playing the audio after the focus gained otherwise do nothing
     * because user himself paused the player so it makes no sense to continue playing as it was already paused
     */
    private var prevPlayerState = false

    //var indicates if the focus is permanently lost so we can request focus again
    private var isFocusPermanentLost = false

    companion object {
        fun create(application: Context, lifecycleOwner: LifecycleOwner): AudioPlayer {
            return AudioPlayer(application, lifecycleOwner)
        }
    }

    //creating concatenating media source for media player to play_notification
    private fun buildMediaSource(audioUris: ArrayList<Uri>?): MediaSource? {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(application,
                Util.getUserAgent(application, "MediaPlayer"))
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
        player!!.apply {
            //to control to player the audio or video right now or wait user to play_collapsed_notification the audio himself
            playWhenReady = true
            val mediaSource = buildMediaSource(mSongList)
            prepare(mediaSource)
            //to control the starter location of audio and current track
            seekTo(chosenAudioIndex)
            application.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
        }
    }

    fun startPlayer(audioList: ArrayList<Uri>, chosenAudioIndex: Int) {
        //only re setup the player when the playlist changes
        if (audioList != mSongList) {
            mSongList = audioList
            setUpPlayer(chosenAudioIndex)
        } else {
            seekTo(chosenAudioIndex)
            //if the player was being stopped then play
            if (!isPlaying) {
                play()
            }
        }
        requestFocus()
        //trigger these callback for first time every time player is being started
        onPlayerStateChanged?.onRepeatModeChanged(player!!.repeatMode)
        onPlayerStateChanged?.onShuffleModeChanged(player!!.shuffleModeEnabled)
    }


    init {
        lifecycleOwner.lifecycle.addObserver(this)
        setOnPlayerStateChanged()
    }

    //listener for the events happend to the player to observe
    fun setOnPlayerStateChanged(onPlayerStateChanged: OnPlayerStateChanged?) {
        this.onPlayerStateChanged = onPlayerStateChanged
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
                            onPlayerStateChanged?.onAudioChanged(currentAudioIndex, isPlaying)
                            setDuration()


                        }

                    }
                }


                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    Log.v("duartionplayer", " duration :statechanged")

                    when {
                        isPlayerStopped() -> {
                            //when player stop we stop listening to plug off events of headphone because player is already stopped
                            application.unregisterReceiver(myNoisyAudioStreamReceiver)
                            isPlaying = false
                            onPlayerStateChanged?.onPause()

                        }
                        isPlayerPlaying() -> {
                            Log.v("duartionplayer", " duration :isplaying")

                            if (isFocusPermanentLost)
                                requestFocus()
                            //when player start again we start listening to  events of headphone
                            application.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                            isPlaying = true
                            onPlayerStateChanged?.onPlay()


                        }
                        playbackState == ExoPlayer.STATE_ENDED -> {
                            onPlayerStateChanged?.onAudioListCompleted()

                        }


                    }
                }


                override fun onRepeatModeChanged(repeatMode: Int) {
                    onPlayerStateChanged?.onRepeatModeChanged(repeatMode)
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    onPlayerStateChanged?.onShuffleModeChanged(shuffleModeEnabled)
                }
            })
        }
    }

    /**
     * request focus for audio player to start
     */
    private fun requestFocus() {
        mMediaAudioFocus.requestAudioFocus(object : AudioFocusCallBacks {
            //when the focus gained we start playing audio if it was previously running
            override fun onAudioFocusGained() {
                if (prevPlayerState)
                    play()

            }

            //when the focus lost we pause the player and set prevPlayerState to false
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
                onPlayerStateChanged?.onDurationChange(player?.duration!!)
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
        onPlayerStateChanged?.onAudioChanged(currentAudioIndex, isPlaying)
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
        if (::runnable.isInitialized)
            handler.post(runnable)
        player?.playWhenReady = true
    }

    /**
     * pause audio and remove runnable callback of Audio progress if it is initialized
     */
    fun pause() {
        if (::runnable.isInitialized)
            handler.removeCallbacks(runnable)
        player?.playWhenReady = false
    }

    /**
     * go to next audio
     */
    fun next() {
        player?.next()
    }

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    fun previous() {
        player?.apply {
            when {
                currentPosition > 3000 -> seekTo(0)
                else -> previous()
            }
        }
    }

    /**
     * position of current played audio in long val
     */
    fun currentPosition() = player!!.currentPosition

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
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun release() {
        player?.let {
            player!!.release()
            player = null
        }
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

    //detect if player already playing

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


    /**
     *listener for progress of current playing audio
     * we can use it in seek bar to continuously display timer
     * also we have to provide lifecycle owner so we can remove callback of handler when the lifecycle owner has been paused
     */
    inner class AudioProgress : LifecycleObserver {
        fun setOnProgressChanged(lifecycleOwner: LifecycleOwner, progressChanged: (duration: Long, progress: Long) -> Unit) {
            lifecycleOwner.lifecycle.addObserver(this)
            //remove any previous callback
            removeCallback()
            handler = Handler()
            runnable = Runnable {
                progressChanged(player!!.duration, currentPosition())
                //update the text position under seek bar to reflect the current position of seek bar
                handler.postDelayed(runnable, 50)

            }
            handler.postDelayed(runnable, 50)
        }

        private fun removeCallback() {
            if (::runnable.isInitialized) {
                handler.removeCallbacks(runnable)
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        private fun onPause() {

            removeCallback()

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        private fun onResume() {
            if (::runnable.isInitialized) {
                handler.post(runnable)
            }
        }
    }

}

interface OnPlayerStateChanged {

    /**
     * this will be called when the player complete playing all the the audio file in the list and no other files to play
     */
    fun onAudioListCompleted()

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
    fun onShuffleModeChanged(enable: Boolean)

    fun onRepeatModeChanged(repeatMode: Int)

    fun onDurationChange(duration: Long) {}


}