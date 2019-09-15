package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.NOTIFICATION_ID
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.foregroundService.audioFocus.AudioFocusCallBacks
import com.example.mediaplayer.foregroundService.audioFocus.MediaAudioFocusCompat
import com.example.mediaplayer.foregroundService.audioFocus.MediaAudioFocusCompatFactory
import com.example.mediaplayer.ui.chosenSong.MediaInfo


class ChosenSongService : Service() {
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


    override fun onCreate() {
        // The service is being created.
        mNotificationManager = NotificationManagerCompat.from(this@ChosenSongService)
        application = applicationContext
        mediaAudioFocus = MediaAudioFocusCompatFactory.create(applicationContext)
        audioPlayer = AudioPlayer.create(applicationContext)

    }


    //handle the player when actions happen in notification
    private fun handlePlayerEvent() {
        audioPlayer.setOnPlayerStateChanged(object : OnPlayerStateChanged {
            override fun onAudioStateChanged() {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())
            }

        })
    }


    internal inner class SongBinder : Binder() {
        val service: ChosenSongService
            get() = this@ChosenSongService

    }

    private fun getNotification(): Notification {
        return foregroundNotification.build(audioPlayer.isPlaying, audioPlayer.currentAudioIndex)
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

            override fun onAudioFocusLost() {
                prevPlayerState = audioPlayer.isPlaying
                audioPlayer.pause()

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
                        //so we do not setup the player again when any configurations happen
                        if (!isServiceSetuped) {
                            audioPlayer.setUpPlayer(playListModels!!, chosenSongIndex)
                            isServiceSetuped = true
                        } else
                            audioPlayer.seekTo(chosenSongIndex)
                        requestFocus()
                        startForeground(NOTIFICATION_ID, getNotification())
                    }

                }
                PlayerActions.PAUSE_ACTION.value -> pausePlayer()
                PlayerActions.PLAY_ACTION.value -> startPlayer()
                PlayerActions.PREVIOUS_ACTION.value -> audioPlayer.previous()
                PlayerActions.NEXT_ACTION.value -> audioPlayer.next()
                PlayerActions.DELETE_ACTION.value -> stopSelf()
            }
        }
    }

    private fun startPlayer() {
        requestFocus()
        //we start foreground again because we stop it in pause action
        startForeground(NOTIFICATION_ID, getNotification())
        audioPlayer.play()
    }

    private fun pausePlayer() {
        audioPlayer.pause()
        //stop the foreground mode so if user can cancel the the media notification by swiping it away
        // as a result ,delete intent will be triggered so we kill the service
        stopForeground(false)
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