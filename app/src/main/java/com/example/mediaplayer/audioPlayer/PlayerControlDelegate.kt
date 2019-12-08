package com.example.mediaplayer.audioPlayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.example.mediaplayer.CustomScope
import com.example.mediaplayer.audioPlayer.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompatFactory
import com.example.mediaplayer.model.SongModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class PlayerControlDelegate(private val context: Context,
                                 private var player: SimpleExoPlayer?
) :
        IPlayerControl, CoroutineScope by CustomScope() {

    private lateinit var songList: ArrayList<SongModel>
    private val songListUris: ArrayList<Uri> = ArrayList()
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

    /**
     * variable to indicate to the last state of player if audio focus happened
     * so if the last state of player was true then continue playing the audio after the focus gained otherwise do nothing
     * because user himself paused the player so it makes no sense to continue playing as it was already paused
     */
    private var prevPlayerState = false


    //var indicates if the focus is permanently lost so we can request focus again
    private var isFocusPermanentLost = false


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
        player?.apply {
            //to control to player the audio or video right now or wait user to play_collapsed_notification the audio himself
            playWhenReady = true
            val mediaSource = buildMediaSource(songListUris)
            prepare(mediaSource)
            //to control the starter location of audio and current track
            seekTo(chosenAudioIndex)

        }
    }


    override fun startPlayer(audioList: ArrayList<SongModel>, chosenAudioIndex: Int) {
        //only re setup the player when the playlist changes
        if (!::songList.isInitialized || audioList != songList) {
            songList = audioList
            for (item in audioList) {
                songListUris.add(item.audioUri)
            }
            setUpPlayer(chosenAudioIndex)
        } else {
            seekTo(chosenAudioIndex)
            //if the player was being stopped then play
            if (!player!!.playWhenReady) {
                Handler().postDelayed({
                    play()
                }, 1000)

            }
        }
        requestFocus()

    }

    /**
     * request focus for audio player to start
     */
    override fun requestFocus() {
        mMediaAudioFocus.requestAudioFocus(object : AudioFocusCallBacks {
            //when the focus gained we start playing audio if it was previously running
            override fun onAudioFocusGained() {
                if (prevPlayerState) {
                    play()
                }

            }

            //when the focus lost we pause the player and set prevPlayerState to the current state of player
            override fun onAudioFocusLost(Permanent: Boolean) {
                prevPlayerState = player!!.playWhenReady
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
        launch {
            // iNotification.update(songList[index], player!!.playWhenReady)
            player?.seekTo(index, 0)

            if (!player!!.playWhenReady)
                play()
        }


    }


    /**
     * seek to different position
     */
    override fun seekToSecond(second: Int) {
        player?.seekTo(second * 1000.toLong())
    }

    /**
     * play audio and reset runnable callback of Audio progress if it was initialized before
     */
    override fun play() {
        player?.playWhenReady = true
    }

    /**
     * pause audio and remove runnable callback of Audio progress if it is initialized
     */
    override fun pause() {
        player?.playWhenReady = false
    }

    /**
     * go to next audio
     */
    override fun next(dispatchEvent: Boolean) {
        launch {
            // iNotification.update(songList[player!!.currentWindowIndex], player!!.playWhenReady)
            if (dispatchEvent)
                player?.next()
        }

    }

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    override fun previous(dispatchEvent: Boolean) {
        launch {
            // iNotification.update(songList[player!!.currentWindowIndex], player!!.playWhenReady)
            if (dispatchEvent) {
                player?.apply {
                    when {
                        currentPosition > 3000 -> seekTo(0)
                        else -> previous()
                    }
                }
            }

        }

    }

    /**
     * change the audio state from playing to pausing and vice verse
     */
    override fun changeAudioState(dispatchEvent: Boolean) {
        launch {
            if (dispatchEvent) {
                if (player!!.playWhenReady) {
                    pause()
                } else {
                    play()
                }
            }
            //iNotification.update(songList[player!!.currentWindowIndex], player!!.playWhenReady)

        }

    }


}