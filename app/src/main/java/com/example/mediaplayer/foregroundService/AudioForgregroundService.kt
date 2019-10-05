package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.AudioPlayer.AudioPlayer
import com.example.mediaplayer.AudioPlayer.OnPlayerStateChanged
import com.example.mediaplayer.AudioPlayer.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.AudioPlayer.audioFocus.MediaAudioFocusCompat
import com.example.mediaplayer.AudioPlayer.audioFocus.MediaAudioFocusCompatFactory
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.NOTIFICATION_ID
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.ui.chosenSong.MediaInfo


class AudioForgregroundService : Service() {
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
    private var isServiceSetuped = false
    private lateinit var mediaAudioFocus: MediaAudioFocusCompat
    /**
     * variable to indicate to the last state of player if audio focus happened
     * so if the last state of player was true then continue playing the audio after the focus gained otherwise do nothing
     * because user himself paused the player so it makes no sense to continue playing as it was already paused
     */
    private var prevPlayerState = false
    //var indicates if the focus is permanently lost so we can request focus again
    private var isFocusPermanentLost = false


    override fun onCreate() {
        // The service is being created.
        mNotificationManager = NotificationManagerCompat.from(this@AudioForgregroundService)
        application = applicationContext
        mediaAudioFocus = MediaAudioFocusCompatFactory.create(applicationContext)
        audioPlayer = AudioPlayer.create(applicationContext)

    }


    private fun setNotification(): Notification {
        return foregroundNotification.build(audioPlayer.isPlaying, audioPlayer.currentAudioIndex)
    }

    //handle the player when actions happen in notification
    private fun handlePlayerEvent() {
        audioPlayer.setOnPlayerStateChanged(object : OnPlayerStateChanged {
            override fun onAudioChanged() {
                mNotificationManager.notify(NOTIFICATION_ID, setNotification())
            }

            override fun onPlay() {
                if (isFocusPermanentLost)
                    requestFocus()
                startForeground(NOTIFICATION_ID, setNotification())


            }

            override fun onStop() {
                mNotificationManager.notify(NOTIFICATION_ID, setNotification())

            }
        })
    }


    internal inner class SongBinder : Binder() {
        val service: AudioForgregroundService
            get() = this@AudioForgregroundService

    }

    /**data class can be founded in chosenSongFragment*/
    private fun intentData(intent: Intent): MediaInfo {
        //playList of songs
        //getting the current playing song index
        intent.run {
            return MediaInfo(getParcelableArrayListExtra(LIST_SONG)!!, getIntExtra(CHOSEN_SONG_INDEX, 0))
        }
    }

    private fun requestFocus() {
        mediaAudioFocus.requestAudioFocus(object : AudioFocusCallBacks {
            override fun onAudioFocusGained() {
                if (prevPlayerState)
                    audioPlayer.play()

            }

            override fun onAudioFocusLost(Permanent: Boolean) {
                prevPlayerState = audioPlayer.isPlaying
                audioPlayer.pause()
                isFocusPermanentLost = Permanent
            }
        })
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                PlayerActions.ACTION_FOREGROUND.value -> {
                    with(intentData(it))
                    {
                        foregroundNotification = ForegroundNotification(playListModels, application)
                        handlePlayerEvent()
                        //so we do not setup the player again when any configurations happen in activity
                        if (!isServiceSetuped) {
                            audioPlayer.setUpPlayer(playListModels!!, chosenSongIndex)
                            isServiceSetuped = true
                        } else {
                            audioPlayer.seekTo(chosenSongIndex)
                            //if the player was being stopped then play
                            if (!audioPlayer.isPlaying) {
                                audioPlayer.play()
                            }
                        }
                        requestFocus()
                        startForeground(NOTIFICATION_ID, setNotification())
                    }

                }
                PlayerActions.PAUSE_ACTION.value, AudioManager.ACTION_AUDIO_BECOMING_NOISY -> audioPlayer.pause()
                PlayerActions.PLAY_ACTION.value -> audioPlayer.play()
                PlayerActions.PREVIOUS_ACTION.value -> audioPlayer.previous()
                PlayerActions.NEXT_ACTION.value -> audioPlayer.next()
                PlayerActions.DELETE_ACTION.value -> {
                    //remove the notification and stop the service when user press the close button on notification
                    stopForeground(false)
                    audioPlayer.pause()
                    mNotificationManager.cancelAll()
                    stopSelf()
                }

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
        audioPlayer.release()
        super.onDestroy()
    }

}