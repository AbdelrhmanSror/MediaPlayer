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
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.OnPlayerStateChanged
import com.example.mediaplayer.model.SongModel


data class MediaInfo(var songList: ArrayList<SongModel>? = arrayListOf(),
                     var chosenSongIndex: Int = 0)

class AudioForegroundService : Service(), OnPlayerStateChanged {


    lateinit var audioPlayer: AudioPlayer
        private set

    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY
    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    private lateinit var foregroundNotification: ForegroundNotification
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

    fun registerObserver(onPlayerStateChanged: OnPlayerStateChanged, enableProgress: Boolean) {
        audioPlayer.registerObserver(onPlayerStateChanged, enableProgress)
    }

    fun removeObserver(onPlayerStateChanged: OnPlayerStateChanged, enableProgress: Boolean) {
        audioPlayer.removeObserver(onPlayerStateChanged, enableProgress)
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

    /**data class can be founded in chosenSongFragment*/
    private fun intentData(intent: Intent): MediaInfo {
        //playList of songs
        //getting the current playing song index
        intent.run {
            return MediaInfo(getParcelableArrayListExtra(LIST_SONG)!!, getIntExtra(CHOSEN_SONG_INDEX, 0))
        }
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
                PlayerActions.ACTION_FOREGROUND.value -> {
                    setUpPlayerForeground(intent)
                }
                PlayerActions.PAUSE_ACTION.value, AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    audioPlayer.pause()
                }
                PlayerActions.PLAY_ACTION.value -> {
                    audioPlayer.play()
                }
                PlayerActions.PREVIOUS_ACTION.value ->
                    audioPlayer.previous()
                PlayerActions.NEXT_ACTION.value ->
                    audioPlayer.next()
                PlayerActions.DELETE_ACTION.value -> {
                    cancelForeground()
                }

            }
        }
    }

    private fun setUpPlayerForeground(intent: Intent) {
        with(intentData(intent))
        {
            val songListUris: ArrayList<Uri> = ArrayList()
            for (item in songList as ArrayList) {
                songListUris.add(item.audioUri)
            }
            foregroundNotification = ForegroundNotification(songList, applicationContext)
            audioPlayer.startPlayer(songListUris, chosenSongIndex)
            startForeground(NOTIFICATION_ID, getNotification())
        }

    }

    private fun cancelForeground() {
        //remove the notification and stop the service when user press the close button on notification
        stopForeground(false)
        mNotificationManager.cancelAll()
        stopSelf()
    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
        mNotificationManager.notify(NOTIFICATION_ID, getNotification())


    }

    override fun onPlay() {
        startForeground(NOTIFICATION_ID, getNotification())


    }

    override fun onPause() {
        mNotificationManager.notify(NOTIFICATION_ID, getNotification())


    }

    override fun onAudioListCompleted() {
        audioPlayer.pause()
        seekTo(0)

    }


    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}

