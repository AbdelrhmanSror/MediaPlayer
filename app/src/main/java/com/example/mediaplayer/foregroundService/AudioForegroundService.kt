package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.example.mediaplayer.*
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.OnPlayerStateChanged
import com.example.mediaplayer.ui.chosenSong.MediaInfo


class AudioForegroundService : LifecycleService() {


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

    private var serviceAudioPlayerObserver: ServiceAudioPlayerObserver? = null

    override fun onCreate() {
        super.onCreate()
        Log.v("audioServicePlayer", "created")
        // The service is being created.
        mNotificationManager = NotificationManagerCompat.from(this@AudioForegroundService)
        audioPlayer = AudioPlayer.create(applicationContext, this).apply {
            setOnPlayerStateChanged(onPlayerStateChanged())
        }


    }


    fun setOnServiceAudioChangeListener(serviceAudioPlayerObserver: ServiceAudioPlayerObserver?) {
        this.serviceAudioPlayerObserver = serviceAudioPlayerObserver
        /**
         * trigger callback for first time to update ui,
         * this case for entering the app from notification
         * as audio player already setuped the player callback won't trigger so we have to do it manually
         */
        with(audioPlayer) {
            serviceAudioPlayerObserver?.onAudioChanged(currentAudioIndex, isPlaying)
            serviceAudioPlayerObserver?.onShuffleModeChanged(playerShuffleMode)
            serviceAudioPlayerObserver?.onRepeatModeChanged(playerRepeatMode)
            serviceAudioPlayerObserver?.onDurationChange(playerDuration)
        }

    }


    //handle the player when actions happen in notification
    private fun onPlayerStateChanged(): OnPlayerStateChanged {
        return object : OnPlayerStateChanged {
            override fun onAudioChanged(index: Int, isPlaying: Boolean) {
                serviceAudioPlayerObserver?.onAudioChanged(index, isPlaying)
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())


            }

            override fun onPlay() {
                serviceAudioPlayerObserver?.onPlay()
                startForeground(NOTIFICATION_ID, getNotification())


            }

            override fun onPause() {
                serviceAudioPlayerObserver?.onPause()
                mNotificationManager.notify(NOTIFICATION_ID, getNotification())


            }

            override fun onShuffleModeChanged(enable: Boolean) {
                serviceAudioPlayerObserver?.onShuffleModeChanged(enable)

            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                serviceAudioPlayerObserver?.onRepeatModeChanged(repeatMode)
            }

            override fun onAudioListCompleted() {
                audioPlayer.pause()
                seekTo(0)

            }

            override fun onDurationChange(duration: Long) {
                serviceAudioPlayerObserver?.onDurationChange(duration)

            }
        }
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
        super.onBind(intent)
        Log.v("serviceaudioPlayer", "bind")
        return mBinder
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
            foregroundNotification = ForegroundNotification(songList, applicationContext)
            audioPlayer.startPlayer(songListUris, chosenSongIndex)
            // setServiceState(true)
            startForeground(NOTIFICATION_ID, getNotification())
        }

    }

    private fun cancelForeground() {
        //remove the notification and stop the service when user press the close button on notification
        stopForeground(false)
        mNotificationManager.cancelAll()
        stopSelf()
    }


    override fun onDestroy() {
        setServiceState(false)
        super.onDestroy()
    }

    private fun setServiceState(isRunning: Boolean) {
        val sharedPref = application.getSharedPreferences(SERVICE_STATE, Context.MODE_PRIVATE)
                ?: return
        with(sharedPref.edit()) {
            putBoolean(SERVICE_STATE, isRunning)
            apply()
        }
    }

    private fun getServiceState(): Boolean {
        val sharedPref = application.getSharedPreferences(SERVICE_STATE, Context.MODE_PRIVATE)
        Log.v("servocestate", "${sharedPref.getBoolean(SERVICE_STATE, false)}")
        return sharedPref.getBoolean(SERVICE_STATE, false)
    }

}


interface ServiceAudioPlayerObserver {


    /**
     * this triggers whenever the audio track changes or when the player state changes like play and pause
     */
    fun onAudioChanged(chosenSongIndex: Int, isPlaying: Boolean)

    /**
     * this triggers whenever the audio start  playing
     */
    fun onPlay()

    /**
     * this triggers whenever the audio stop playing
     */
    fun onPause()

    /**
     * this triggers whenever the audio shuffle and repeat mode changes changes
     * also it triggers at initialization time
     */
    fun onShuffleModeChanged(enable: Boolean)

    fun onRepeatModeChanged(repeatMode: Int)

    fun onDurationChange(duration: Long)


}