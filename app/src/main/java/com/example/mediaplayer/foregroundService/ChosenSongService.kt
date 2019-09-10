package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.*
import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.ui.chosenSong.MediaInfo
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.*

class ChosenSongService : Service() {

    lateinit var player: SimpleExoPlayer
        private set
    private var mPlayWhenReady = true
    private var mCurrentWindowIndex = 0
    private lateinit var application: Context

    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY

    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    private lateinit var foregroundNotification: ForegroundNotification
    //responsible for updating the notification
    private lateinit var mNotificationManager: NotificationManagerCompat
    private var isServiceSetuped = false


    override fun onCreate() {
        Log.v("mediaService", "serviceCreated")

        // The service is being created.
        player = ExoPlayerFactory.newSimpleInstance(applicationContext)
        mNotificationManager = NotificationManagerCompat.from(this@ChosenSongService)
        application = applicationContext

    }

    //creating concatenating media source for media player to play
    private fun buildMediaSource(audioUris: ArrayList<PlayListModel>?): MediaSource? {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(application,
                Util.getUserAgent(application, "MediaPlayer"))
        val concatenatingMediaSource = ConcatenatingMediaSource()
        if (audioUris == null) {
            return null
        } else {
            for (item in audioUris) {
                concatenatingMediaSource.addMediaSource(ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(item.audioUri))
            }
        }
        return concatenatingMediaSource
    }

    fun setUpPlayer(audioUris: ArrayList<PlayListModel>, chosenSongIndex: Int) {
        player.run {
            //to control to player the audio or video right now or wait user to play the audio himself
            playWhenReady = true
            val mediaSource = buildMediaSource(audioUris)
            prepare(mediaSource)
            //to control the starter location of audio
            seekTo(chosenSongIndex, 0)
            //to handle audio focus changes
            setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(), true)
        }
    }

    //handle the player when actions happen in notification
    private fun handlePlayerEvent() {
        player.run {
            addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when {
                        isPlayerStopped() -> {
                            mPlayWhenReady = false

                        }
                        isPlayerPlaying() -> {
                            mPlayWhenReady = true

                        }
                        isPlayerNext() -> {
                            mCurrentWindowIndex = currentWindowIndex

                        }
                        isPlayerPrevious() -> {
                            mCurrentWindowIndex = currentWindowIndex
                        }
                    }
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification())
                }
            })
        }
    }

    private fun isPlayerStopped(): Boolean {
        player.run {
            return playWhenReady != mPlayWhenReady && !playWhenReady
        }
    }

    private fun isPlayerPlaying(): Boolean {
        player.run {
            return playWhenReady != mPlayWhenReady && playWhenReady

        }
    }

    private fun isPlayerNext(): Boolean {
        player.run {
            return currentWindowIndex != mCurrentWindowIndex && currentWindowIndex < mCurrentWindowIndex

        }
    }

    private fun isPlayerPrevious(): Boolean {
        player.run {
            return currentWindowIndex != mCurrentWindowIndex && currentWindowIndex > mCurrentWindowIndex

        }
    }


    internal inner class SongBinder : Binder() {
        val service: ChosenSongService
            get() = this@ChosenSongService

    }

    private fun getNotification(): Notification {
        return foregroundNotification.build(mPlayWhenReady, mCurrentWindowIndex)
    }


    /**data class can be founded in chosenSongFragment*/
    private fun intentData(intent: Intent): MediaInfo {
        //playList of songs
        //getting the current playing song index
        intent.run {
            return MediaInfo(getParcelableArrayListExtra(LIST_SONG)!!, getIntExtra(CHOSEN_SONG_INDEX, 0))
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_FOREGROUND -> {
                    with(intentData(it))
                    {
                        mCurrentWindowIndex = chosenSongIndex
                        foregroundNotification = ForegroundNotification(playListModels, application)
                        handlePlayerEvent()
                        //so we do not setup the player again when any configurations happen
                        if (!isServiceSetuped) {
                            setUpPlayer(playListModels!!, chosenSongIndex)
                            isServiceSetuped = true
                        }
                        startForeground(NOTIFICATION_ID, getNotification())
                    }

                }
                PAUSE_ACTION -> {
                    player.playWhenReady = false
                    //stop the foreground mode so if user can cancel the the media notification by swiping it away
                    // as a result ,delete intent will be triggered so we kill the service
                    stopForeground(false)
                }
                PLAY_ACTION -> player.playWhenReady = true
                PREVIOUS_ACTION -> player.previous()
                NEXT_ACTION -> player.next()
                DELETE_ACTION -> stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?,
                                flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService().
        handleIntent(intent)
        return mStartMode
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
