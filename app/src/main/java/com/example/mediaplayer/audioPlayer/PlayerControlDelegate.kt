package com.example.mediaplayer.audioPlayer

import android.content.Context
import android.net.Uri
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.extensions.isPlayerStateEnded
import com.example.mediaplayer.extensions.isPlayerStopping
import com.example.mediaplayer.extensions.isPlaying
import com.example.mediaplayer.shared.CustomScope
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


internal open class PlayerControlDelegate(private val context: Context,
                                          private var player: SimpleExoPlayer?,
                                          private val mediaPreferences: MediaPreferences
) : IPlayerControl, CoroutineScope by CustomScope(Dispatchers.Main) {


    private var songList: List<Any>? = null
    private var songListUris: List<Uri> = emptyList()
    private var currentIndex = 0
    private var currentPosition = 0L
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
        seekToIndex(index)
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
    override fun seekToIndex(index: Int) {
        currentIndex = index
        player?.seekTo(index, 0)
        player!!.playWhenReady = true
        retryIfStopped()

    }


    /**
     * seek to different position
     */
    override fun seekToSecond(second: Int) {
        player?.seekTo(currentIndex, second * 1000.toLong())
        retryIfStopped()

    }


    private fun retryIfStopped(retry: Boolean = true, action: ((index: Int, position: Long) -> Unit)? = null): Boolean {
        with(player!!) {
            if (isPlayerStopping()) {
                action?.invoke(mediaPreferences.getCurrentTrack(), mediaPreferences.getCurrentPosition())
                if (::mediaSource.isInitialized && retry)
                    prepare(mediaSource, false, false)
                return true
            }
            return false
        }
    }

    override fun currentIndex(): Int {
        val stopped = retryIfStopped(false) { index, _ ->
            currentIndex = index
        }
        return if (!stopped) player!!.currentWindowIndex
        else currentIndex
    }

    override fun currentPosition(): Long {
        val stopped = retryIfStopped(false) { _, position ->
            currentPosition = position
        }
        return if (!stopped) player!!.currentPosition
        else currentPosition
    }

    override fun currentTag(): Any? {
        with(player!!) {
            return if (currentTag == null) {
                if (songList != null)
                    songList?.get(currentIndex)
                else null
            } else currentTag
        }
    }

    /**
     * play audio
     */
    override fun play() {
        with(player!!) {
            if (!isPlaying()) {
                //for when player finish playing all tracks then the state will be ended so if user clicked play we repeat the same song again
                if (isPlayerStateEnded())
                    seekTo(currentIndex, 0)
                playWhenReady = true
            }
        }
    }

    /**
     * pause audio
     */
    override fun pause() {
        with(player!!) {
            if (isPlaying()) {
                playWhenReady = false
            }
        }
    }

    /**
     * go to next audio
     */
    override fun next() {
        with(player!!) {
            val stopped = retryIfStopped { index, _ ->
                currentIndex = index
                seekTo(--currentIndex, 0)
                playWhenReady = true
            }
            if (!stopped) {
                currentIndex++
                next()
            }
        }

    }

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    override fun previous() {
        with(player!!) {
            val stopped = retryIfStopped { index, _ ->
                currentIndex = index
                seekTo(--currentIndex, 0)
                playWhenReady = true
            }
            if (!stopped) {
                when {
                    currentPosition > 3000 -> seekTo(0)
                    else -> {
                        --currentIndex
                        previous()
                    }
                }
            }
        }

    }

    /**
     * change the audio state from playing to pausing and vice verse
     * to change the current state always use this method, if u tried to use play or pause method will cause unwanted behaviour
     */
    override fun changeAudioState() {
        with(player!!) {
            val stopped = retryIfStopped { index, position ->
                seekTo(index, position)
                playWhenReady = true
            }
            if (!stopped) {
                if (isPlaying()) {
                    pause()
                } else {
                    play()
                }
            }
        }
    }


}

