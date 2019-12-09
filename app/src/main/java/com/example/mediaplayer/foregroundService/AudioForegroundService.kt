package com.example.mediaplayer.foregroundService

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleService
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.CustomScope
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.NOTIFICATION_ID
import com.example.mediaplayer.PlayerActions.ACTION_FOREGROUND
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.IpLayerState
import com.example.mediaplayer.audioPlayer.notification.AudioForegroundNotification
import com.example.mediaplayer.di.inject
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.getMediaDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject


class AudioForegroundService @Inject constructor() : LifecycleService(), IpLayerState, CoroutineScope by CustomScope() {
    @Inject
    lateinit var mediaSession: MediaSessionCompat

    @Inject
    lateinit var audioPlayer: AudioPlayer
    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY
    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    @Inject
    lateinit var foregroundNotification: AudioForegroundNotification

    override fun onCreate() {
        super.onCreate()
        this.inject()
        // The service is being created.
        audioPlayer.registerObserver(this@AudioForegroundService)
        registerObserver(this)


    }


    fun registerObserver(ipLayerState: IpLayerState) {
        audioPlayer.apply {
            registerObserver(ipLayerState)
            setAudioSessionChangeListener()
            setOnProgressChangedListener()
            setNoisyListener()
        }
    }

    fun removeObserver(ipLayerState: IpLayerState) {
        audioPlayer.removeObserver(ipLayerState)
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

    fun changeAudioState(dispatchEvent: Boolean = true) {
        /*if (!audioPlayer.isPlaying)
            stopForeground(false)*/
        audioPlayer.changeAudioState(dispatchEvent)

    }

    fun goToPrevious(dispatchEvent: Boolean = true) {
        audioPlayer.previous(dispatchEvent)

    }

    fun goToNext(dispatchEvent: Boolean = true) {
        audioPlayer.next(dispatchEvent)

    }

    fun seekTo(index: Int) {
        audioPlayer.seekTo(index)

    }

    override fun onStop() {
        //remove the notification and stop the service when user press the close button on notification
        audioPlayer.pause()
        foregroundNotification.cancel()
        audioPlayer.release { stopSelf() }
    }

    internal inner class SongBinder : Binder() {
        val service: AudioForegroundService
            get() = this@AudioForegroundService

    }


    override fun onStartCommand(intent: Intent?,
                                flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleIntent(intent)

        // The service is starting, due to a call to startService().
        return mStartMode
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_FOREGROUND -> {
                    setUpPlayerForeground(intent)
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
            audioPlayer.startPlayer(first, second)
            audioPlayer.enableCommandControl { index ->
                first[index].getMediaDescription()
            }
            launch {
                startForeground(NOTIFICATION_ID, foregroundNotification.update(first[second], true))
            }
        }

    }

    override fun onPlay() {
        /*  launch {
              foregroundNotification.update(playerMediaObjects[audioPlayer.currentAudioIndex], true)
          }*/
    }

    override fun onPause() {
        //stopForeground(false)
        /* launch {
             foregroundNotification.update(playerMediaObjects[audioPlayer.currentAudioIndex], false)
         }*/
    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
        /* launch {
             foregroundNotification.update(playerMediaObjects[index], isPlaying)
         }*/
    }

    override fun onAudioListCompleted() {
        audioPlayer.pause()
        seekTo(0)

    }

}

