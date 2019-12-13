package com.example.mediaplayer.foregroundService

import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.audioPlayer.notification.AudioForegroundNotificationManager
import com.example.mediaplayer.di.inject
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.getMediaDescription
import com.example.mediaplayer.shared.CHOSEN_SONG_INDEX
import com.example.mediaplayer.shared.CustomScope
import com.example.mediaplayer.shared.LIST_SONG
import com.example.mediaplayer.shared.PlayerActions.ACTION_FOREGROUND
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject


class AudioForegroundService @Inject constructor() : LifecycleService(),
        IPlayerState<SongModel>,
        CoroutineScope by CustomScope() {
    @Inject
    lateinit var mediaSession: MediaSessionCompat
    @Inject
    lateinit var audioPlayer: AudioPlayer<SongModel>
    // indicates how to behave if the service is killed.
    private var mStartMode = START_STICKY
    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    @Inject
    lateinit var notificationManager: AudioForegroundNotificationManager


    override fun onCreate() {
        super.onCreate()
        this.inject()
        // The service is being created.
        audioPlayer.registerObserver(this)

    }


    fun registerObserver(iPlayerState: IPlayerState<SongModel>) {
        audioPlayer.registerObserver(iPlayerState, audioSessionIdCallbackEnable = true
                , progressCallBackEnabled = true
                , isMainObserver = true)
    }

    fun removeObserver(IPlayerState: IPlayerState<SongModel>) {
        audioPlayer.removeObserver(IPlayerState)
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

    override fun onDestroy() {
        super.onDestroy()
        Log.v("audioManagerNotificati", "ondestoryservice")

    }

    override fun onStop() {
        Log.v("audioManagerNotificati", "onstop")
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
            val songListUris: ArrayList<Uri> = arrayListOf()
            for (item in first) {
                songListUris.add(item.audioUri)
            }
            audioPlayer.startPlayer(first, songListUris, second)
            audioPlayer.setCommandControl { index ->
                first[index].getMediaDescription()
            }

        }

    }


    override fun onAudioListCompleted() {
        audioPlayer.pause()
        seekTo(0)

    }
}

