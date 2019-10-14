package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.NOTIFICATION_ID
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.foregroundService.audioPlayer.AudioPlayer
import com.example.mediaplayer.foregroundService.audioPlayer.OnPlayerStateChanged
import com.example.mediaplayer.ui.chosenSong.MediaInfo


class AudioForegroundService : Service() {
    lateinit var audioPlayer: AudioPlayer
        private set
    private lateinit var application: Context

    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY

    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    private lateinit var foregroundNotification: ForegroundNotification
    //responsible for updating the notification
    private lateinit var mNotificationManager: NotificationManagerCompat

    //whenever the audio track changes this live data will trigger and we observe it in ui component to update ui
    val trackChanged = MutableLiveData<Int>()
    //whenever the audio shuffle changes this live data will trigger and we observe it in ui component to update ui
    val shuffleModeChanged = MutableLiveData<Boolean>()
    //whenever the audio shuffle changes this live data will trigger and we observe it in ui component to update ui
    val repeatModeChanged = MutableLiveData<Int>()
    //whenever the audio state changes (play =true/pause=false) this live data will trigger and we observe it in ui component to update ui
    val playerStateChanged = MutableLiveData<Boolean>()


    override fun onCreate() {
        // The service is being created.
        mNotificationManager = NotificationManagerCompat.from(this@AudioForegroundService)
        application = applicationContext
        audioPlayer = AudioPlayer.create(applicationContext, onPlayerStateChanged())

    }

    fun changeRepeatMode() {
        audioPlayer.repeatModeEnable()
    }

    fun changeShuffleMode() {
        audioPlayer.shuffleModeEnable()
    }

    fun changeAudioState() {
        if (audioPlayer.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
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

    fun isAudioPlaying(): Boolean {
        return audioPlayer.isPlaying
    }

    private fun getNotification(): Notification {
        return foregroundNotification.build(audioPlayer.isPlaying, audioPlayer.currentAudioIndex)
    }

    //handle the player when actions happen in notification
    private fun onPlayerStateChanged(): OnPlayerStateChanged {
        return object : OnPlayerStateChanged {
            override fun onAudioChanged() {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())
                trackChanged.value = audioPlayer.currentAudioIndex
            }

            override fun onPlay() {
                startForeground(NOTIFICATION_ID, getNotification())
                playerStateChanged.value = true


            }

            override fun onPause() {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())
                playerStateChanged.value = false


            }

            override fun onShuffleModeChanged(enable: Boolean) {
                shuffleModeChanged.value = enable
            }

            override fun onRepeatModeChanged(shuffleMode: Int) {
                repeatModeChanged.value = shuffleMode
            }

            override fun onAudioListCompleted() {
                audioPlayer.pause()
                seekTo(0)

            }
        }
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


    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                PlayerActions.ACTION_FOREGROUND.value ->
                    setUpPlayerForeground(intent)
                PlayerActions.PAUSE_ACTION.value, AudioManager.ACTION_AUDIO_BECOMING_NOISY ->
                    audioPlayer.pause()
                PlayerActions.PLAY_ACTION.value ->
                    audioPlayer.play()
                PlayerActions.PREVIOUS_ACTION.value ->
                    audioPlayer.previous()
                PlayerActions.NEXT_ACTION.value ->
                    audioPlayer.next()
                PlayerActions.DELETE_ACTION.value ->
                    cancelForeground()

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
            //only re setup the player when the playlist changes
            foregroundNotification = ForegroundNotification(songList, application)
            audioPlayer.startPlayer(songListUris, chosenSongIndex)
            startForeground(NOTIFICATION_ID, getNotification())
        }

    }

    private fun cancelForeground() {
        audioPlayer.release()
        //remove the notification and stop the service when user press the close button on notification
        stopForeground(false)
        mNotificationManager.cancelAll()
        stopSelf()
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
        audioPlayer.release()
        super.onDestroy()
    }

}