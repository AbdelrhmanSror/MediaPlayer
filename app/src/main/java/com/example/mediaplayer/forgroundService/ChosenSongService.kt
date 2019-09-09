package com.example.mediaplayer.forgroundService

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.*
import com.example.mediaplayer.model.PlayListModel
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
    private var currentWindowIndex = 0
    private var application: Context? = null

    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY

    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    lateinit var notificationController: NotificationController
    //responsible for updating the notification
    lateinit var mNotifymanager: NotificationManagerCompat

    private val isPlayerNext: Boolean
        get() = player.currentWindowIndex != currentWindowIndex && player.currentWindowIndex < currentWindowIndex

    private val isPlayerPrevious: Boolean
        get() = player.currentWindowIndex != currentWindowIndex && player.currentWindowIndex > currentWindowIndex

    override fun onCreate() {
        // The service is being created.
        player = ExoPlayerFactory.newSimpleInstance(applicationContext)
        mNotifymanager = NotificationManagerCompat.from(this@ChosenSongService)
        application = applicationContext

    }

    //creating concatenating media source for media player to play
    private fun buildMediaSource(audioUris: ArrayList<PlayListModel>?): MediaSource? {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(application!!,
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
        //to control to player the audio or video right now or wait user to play the audio himself
        player.playWhenReady = true
        val mediaSource = buildMediaSource(audioUris)
        player.prepare(mediaSource)
        //to control the starter location of audio
        player.seekTo(chosenSongIndex, 0)
        //to handle audio focus changes
        player.setAudioAttributes(AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build(), true)


    }

    private fun handlePlayerEvent() {
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when {
                    isPlayerStopped(playWhenReady) -> {
                        mPlayWhenReady = false
                        mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(mPlayWhenReady, currentWindowIndex))

                    }
                    isPlayerPlaying(playWhenReady) -> {
                        mPlayWhenReady = true
                        mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(mPlayWhenReady, currentWindowIndex))


                    }
                    isPlayerNext -> {
                        currentWindowIndex = player.currentWindowIndex
                        mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(player.playWhenReady, currentWindowIndex))


                    }
                    isPlayerPrevious -> {
                        currentWindowIndex = player.currentWindowIndex
                        mNotifymanager.notify(NOTIFICATION_ID, notificationController.buildNotification(player.playWhenReady, currentWindowIndex))

                    }
                }


            }

        })
    }

    private fun isPlayerStopped(playWhenReady: Boolean): Boolean {
        return playWhenReady != mPlayWhenReady && !playWhenReady
    }

    private fun isPlayerPlaying(playWhenReady: Boolean): Boolean {
        return playWhenReady != mPlayWhenReady && playWhenReady
    }


    internal inner class SongBinder : Binder() {
        val service: ChosenSongService
            get() = this@ChosenSongService

    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            when (Objects.requireNonNull<String>(intent.action)) {
                ACTION_FOREGROUND -> {
                    //getting the playlist of song from intent
                    //playList of songs
                    val songList: ArrayList<PlayListModel>? = intent.getParcelableArrayListExtra(LIST_SONG)
                    //getting the current playing song index
                    currentWindowIndex = intent.getIntExtra(CHOSEN_SONG_INDEX, 0)
                    notificationController = NotificationController(songList, this)
                    handlePlayerEvent()
                    startForeground(NOTIFICATION_ID, notificationController.buildNotification(mPlayWhenReady, currentWindowIndex))
                }
                PAUSE_ACTION -> player.playWhenReady = false
                PLAY_ACTION -> player.playWhenReady = true
                PREVIOUS_ACTION -> player.previous()
                NEXT_ACTION -> player.next()
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


}
