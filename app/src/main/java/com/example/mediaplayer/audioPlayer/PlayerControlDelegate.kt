package com.example.mediaplayer.audioPlayer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mediaplayer.audioPlayer.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompat
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.shared.CustomScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


open class PlayerControlDelegate(private val context: Context,
                                 private var player: SimpleExoPlayer?, private val mediaAudioFocusCompat: MediaAudioFocusCompat,
                                 private val mediaPreferences: MediaPreferences
) : IPlayerControl, CoroutineScope by CustomScope(Dispatchers.Main) {


    private var songList: List<Any>? = null
    private var songListUris: List<Uri> = emptyList()
    private var repeatModeActivated: Boolean = false
        set(value) {
            if (value) {
                player?.repeatMode = Player.REPEAT_MODE_ALL
            } else {
                player?.repeatMode = Player.REPEAT_MODE_OFF

            }
            field = value
        }


    private var shuffleModeActivated: Boolean = false
        set(value) {
            player?.shuffleModeEnabled = value
            field = value
        }


    private lateinit var mediaSource: MediaSource
    private var focusLock = false


    /**
     * variable to indicate to the last state of player if audio focus happened
     * so if the last state of player was true then continue playing the audio after the focus gained otherwise do nothing
     * because user himself paused the player so it makes no sense to continue playing as it was already paused
     */
    private var prevPlayerState = false
    private var isFocusLost = true


    //var indicates if the focus is permanently lost so we can request focus again
    private var isFocusPermanentLost = true


    //creating concatenating media source for media player to play_notification
    private fun buildMediaSource(): MediaSource {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "MediaPlayer"))
        val concatenatingMediaSource = ConcatenatingMediaSource()
        for (item in songListUris.indices) {
            concatenatingMediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory).setTag(songList?.get(item))
                    .createMediaSource(songListUris[item]))
        }

        return concatenatingMediaSource
    }

    override fun setUpPlayer(audioList: List<Any>?, audioUris: List<Uri>, index: Int) {
        //only re setup the player when the playlist changes
        if (audioList != songList) {
            songList = audioList
            songListUris = audioUris
            player?.apply {
                mediaSource = buildMediaSource()
                player?.prepare(mediaSource)

            }
        }
        seekTo(index)
    }


    /**
     * request focus for audio player to start
     */
    override fun requestFocus() {
        mediaAudioFocusCompat.requestAudioFocus(object : AudioFocusCallBacks {
            //when the focus gained we start playing audio if it was previously running
            override fun onAudioFocusGained() {
                isFocusLost = false
                if (!focusLock) {
                    launch {
                        focusLock = true
                        delay(1000)
                        Log.v("focusgained", " fgained $prevPlayerState  $isFocusLost")
                        if (prevPlayerState && !isFocusLost) {
                            Log.v("focusgained", "true")
                            play()
                            prevPlayerState = false
                        }
                        focusLock = false
                    }
                }

            }

            //when the focus lost we pause the player and set prevPlayerState to the current state of player
            override fun onAudioFocusLost(Permanent: Boolean) {
                isFocusLost = true
                if (isPlaying()/*&&!prevPlayerState*/) {
                    Log.v("focusgained", " lost $prevPlayerState  $isFocusLost")
                    prevPlayerState = true
                }
                pause()
                isFocusPermanentLost = Permanent
            }
        })
    }

    override fun repeatModeEnable() {
        repeatModeActivated = !repeatModeActivated

    }

    /**
     * enable shuffle mode
     */
    override fun shuffleModeEnable() {
        shuffleModeActivated = !shuffleModeActivated

    }


    /**
     * seek to different track
     */
    override fun seekTo(index: Int) {
        if (!retryIfStopped()) {
            player?.seekTo(index, 0)
            play()
        }
    }


    /**
     * seek to different position
     */
    override fun seekToSecond(second: Int) {
        player?.seekTo(second * 1000.toLong())
    }


    private fun retryIfStopped(): Boolean {
        if (player!!.playbackState == ExoPlayer.STATE_IDLE) {
            player!!.seekTo(mediaPreferences.getCurrentTrack(), mediaPreferences.getCurrentPosition())
            player!!.playWhenReady = true
            player!!.prepare(mediaSource, false, false)
            return true
        }
        return false
    }

    /**
     * play audio
     */
    override fun play() {
        if (isFocusPermanentLost) {
            prevPlayerState = true
            requestFocus()
            isFocusPermanentLost = false
        } else if (!isPlaying()) {
            player?.playWhenReady = true
        }

    }

    private fun isPlaying() = player!!.playWhenReady
    /**
     * pause audio
     */
    override fun pause() {
        if (isPlaying()) {
            player?.playWhenReady = false
        }
    }

    /**
     * go to next audio
     */
    override fun next() {
        player?.next()
    }

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    override fun previous() {
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
    override fun changeAudioState() {
        if (!retryIfStopped()) {
            if (isPlaying()) {
                pause()
            } else {
                play()
            }
        }
    }


}

