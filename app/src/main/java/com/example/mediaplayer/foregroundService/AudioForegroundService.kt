package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.NOTIFICATION_ID
import com.example.mediaplayer.PlayerActions.ACTION_FOREGROUND
import com.example.mediaplayer.PlayerActions.DELETE_ACTION
import com.example.mediaplayer.PlayerActions.NEXT_ACTION
import com.example.mediaplayer.PlayerActions.PAUSE_ACTION
import com.example.mediaplayer.PlayerActions.PLAY_ACTION
import com.example.mediaplayer.PlayerActions.PREVIOUS_ACTION
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.OnPlayerStateChanged
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.getMediaDescription


class AudioForegroundService : Service(), OnPlayerStateChanged {

    private lateinit var audioPlayer: AudioPlayer

    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY
    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    private lateinit var foregroundNotification: AudioForegroundNotification
    //responsible for updating the notification
    private lateinit var mNotificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        // The service is being created.
        mNotificationManager = NotificationManagerCompat.from(this)
        audioPlayer = AudioPlayer.create(applicationContext).apply {
            registerObserver(this@AudioForegroundService, false)
        }


    }


    fun registerObserver(onPlayerStateChanged: OnPlayerStateChanged, instantTrigger: Boolean) {
        audioPlayer.registerObserver(onPlayerStateChanged, instantTrigger)
    }

    fun removeObserver(onPlayerStateChanged: OnPlayerStateChanged) {
        audioPlayer.removeObserver(onPlayerStateChanged)
    }

    fun seekToSecond(second: Int) {
        audioPlayer.seekToSecond(second)
    }

    fun changeRepeatMode() {
        audioPlayer.repeatModeEnable()
    }

    fun changeShuffleMode() {
        audioPlayer.shuffleModeEnable()
    }

    fun changeAudioState() {
        audioPlayer.changeAudioState()
    }

    fun goToPrevious() {
        audioPlayer.previous()
    }

    fun goToNext() {
        audioPlayer.next()
    }

    fun seekTo(index: Int) {
        audioPlayer.seekTo(index)
    }


    private fun getNotification(): Notification {
        return foregroundNotification.build(audioPlayer.isPlaying, audioPlayer.currentAudioIndex)
    }

    internal inner class SongBinder : Binder() {
        val service: AudioForegroundService
            get() = this@AudioForegroundService

    }


    override fun onStartCommand(intent: Intent?,
                                flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // The service is starting, due to a call to startService().
        handleIntent(intent)
        return mStartMode
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_FOREGROUND -> {
                    setUpPlayerForeground(intent)
                }
                PAUSE_ACTION, AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    audioPlayer.pause()
                }
                PLAY_ACTION -> {
                    audioPlayer.play()
                }
                PREVIOUS_ACTION ->
                    audioPlayer.previous()
                NEXT_ACTION ->
                    audioPlayer.next()
                DELETE_ACTION -> {
                    cancelForeground()
                }

            }
        }
    }

    private fun intentData(intent: Intent): Pair<ArrayList<SongModel>, Int> {
        //playList of songs
        //getting the current playing song index
        with(intent) {
            return getParcelableArrayListExtra<SongModel>(LIST_SONG)!! to getIntExtra(CHOSEN_SONG_INDEX, 0)
        }
    }

    private fun setUpPlayerForeground(intent: Intent) {
        with(intentData(intent))
        {
            val songListUris: ArrayList<Uri> = ArrayList()
            for (item in first) {
                songListUris.add(item.audioUri)
            }
            foregroundNotification = AudioForegroundNotification(first, applicationContext)
            audioPlayer.startPlayer(songListUris, second)
            audioPlayer.enableCommandControl { index ->
                first[index].getMediaDescription()
            }
            startForeground(NOTIFICATION_ID, getNotification())
        }

    }

    private fun cancelForeground() {
        //remove the notification and stop the service when user press the close button on notification
        audioPlayer.release {
            stopSelf()
            mNotificationManager.cancelAll()
        }


    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
        mNotificationManager.notify(NOTIFICATION_ID, getNotification())


    }

    override fun onPlay() {
        startForeground(NOTIFICATION_ID, getNotification())
    }

    override fun onPause() {
        stopForeground(false)
        mNotificationManager.notify(NOTIFICATION_ID, getNotification())


    }

    override fun onAudioListCompleted() {
        audioPlayer.pause()
        seekTo(0)

    }

}

