package com.example.mediaplayer.audioPlayer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.mediaplayer.audioPlayer.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompatFactory
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


open class PlayerControlDelegate<T>(private val context: Context,
                                    private var player: SimpleExoPlayer?
) : IPlayerControl<T>, CoroutineScope by CustomScope(Dispatchers.Main) {


    private var songList: ArrayList<T>? = null
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

    private val mMediaAudioFocus = MediaAudioFocusCompatFactory.create(context)

    private lateinit var mediaSource: MediaSource

    /**
     * variable to indicate to the last state of player if audio focus happened
     * so if the last state of player was true then continue playing the audio after the focus gained otherwise do nothing
     * because user himself paused the player so it makes no sense to continue playing as it was already paused
     */
    private var prevPlayerState = false
    private var isFocusLost = false


    companion object {
        var currentWindow = 0
        var playbackPosition: Long = 0
    }

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

    override fun setUpPlayer(audioList: ArrayList<T>?, Uris: List<Uri>, index: Int) {
        //only re setup the player when the playlist changes
        if (audioList != songList) {
            songList = audioList
            songListUris = Uris
            player?.apply {
                mediaSource = buildMediaSource()
                player?.prepare(mediaSource)
                requestFocus()

            }
        }
        seekTo(index)
    }


    /**
     * request focus for audio player to start
     */
    override fun requestFocus() {
        mMediaAudioFocus.requestAudioFocus(object : AudioFocusCallBacks {
            //when the focus gained we start playing audio if it was previously running
            override fun onAudioFocusGained() {
                isFocusLost = false
                launch {
                    delay(1000)
                    if (prevPlayerState && !isFocusLost) {
                        play()
                    }
                }

            }

            //when the focus lost we pause the player and set prevPlayerState to the current state of player
            override fun onAudioFocusLost(Permanent: Boolean) {
                isFocusLost = true
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
        if (!retryIfStopped(index, 0)) {
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


    private fun retryIfStopped(index: Int, playbackPosition: Long): Boolean {
        Log.v("stoopinghandling", "$playbackPosition   $currentWindow")
        if (player!!.playbackState == ExoPlayer.STATE_IDLE) {
            player!!.seekTo(index, playbackPosition)
            player!!.playWhenReady = true
            player!!.prepare(mediaSource, false, false)
            return true
        }
        return false
    }

    /**
     * play audio and reset runnable callback of Audio progress if it was initialized before
     */
    override fun play() {
        if (isFocusPermanentLost) {
            requestFocus()
            isFocusPermanentLost = false
        }
        prevPlayerState = false
        player?.playWhenReady = true

    }

    /**
     * pause audio and remove runnable callback of Audio progress if it is initialized
     */
    override fun pause() {
        prevPlayerState = true
        player?.playWhenReady = false
        currentWindow = player!!.currentWindowIndex
        playbackPosition = player!!.currentPosition
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
        if (!retryIfStopped(currentWindow, playbackPosition)) {
            if (player!!.playWhenReady) {
                pause()
            } else {
                play()
            }
        }
    }


}

