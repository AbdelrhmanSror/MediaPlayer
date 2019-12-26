package com.example.mediaplayer.audioForegroundService

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.IPlayerObserver
import com.example.mediaplayer.audioPlayer.audioFocus.FocusRequestImp
import com.example.mediaplayer.audioPlayer.notification.AudioForegroundNotificationManager
import com.example.mediaplayer.di.inject
import com.example.mediaplayer.intent.CHOSEN_SONG_INDEX
import com.example.mediaplayer.intent.LIST_SONG
import com.example.mediaplayer.intent.NotificationAction
import com.example.mediaplayer.intent.PlayerActions.ACTION_FOREGROUND
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.getMediaDescription
import javax.inject.Inject


class AudioForegroundService @Inject constructor() : LifecycleService() {

    @Inject
    lateinit var audioPlayer: AudioPlayer
    // indicates how to behave if the service is killed.
    private var mStartMode = Service.START_NOT_STICKY
    // interface for clients that bind.
    private var mBinder: IBinder = SongBinder()
    //responsible for creating media player notification;
    @Inject
    lateinit var notificationManager: AudioForegroundNotificationManager
    @Inject
    lateinit var focusRequestImp: FocusRequestImp


    override fun onCreate() {
        inject()
        // The service is being created.
        audioPlayer.registerObservers(notificationManager, focusRequestImp)
        super.onCreate()
    }


    /**
     * for when user clear the recent screen and the player is not playing then we stop the service and clear every thing
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        audioPlayer.releaseIfPossible()

    }


    fun registerObserver(iPlayerObserver: IPlayerObserver) {
        audioPlayer.registerObserver(iPlayerObserver
                , audioSessionIdCallbackEnable = true
                , progressCallBackEnabled = true
                , isMainObserver = true)
    }

    fun removeObserver(IPlayerObserver: IPlayerObserver) {
        audioPlayer.removeObserver(IPlayerObserver)
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
        audioPlayer.seekToIndex(index)

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
        super.onBind(intent)
        return mBinder
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_FOREGROUND -> {
                    setUpPlayerForeground(intent)
                }
                NotificationAction.PLAY_PAUSE -> {
                    audioPlayer.changeAudioState()
                }
                NotificationAction.NEXT -> {
                    audioPlayer.next()
                }
                NotificationAction.PREVIOUS -> {
                    audioPlayer.previous()
                }
                NotificationAction.STOP -> {
                    audioPlayer.releaseIfPossible()
                }

            }
        }
    }

    private fun setUpData(intent: Intent): Triple<ArrayList<SongModel>, ArrayList<Uri>, Int> {
        //playList of songs
        //getting the current playing song index
        with(intent) {
            val songList = getParcelableArrayListExtra<SongModel>(LIST_SONG)!!
            val index = getIntExtra(CHOSEN_SONG_INDEX, 0)
            val songListUris: ArrayList<Uri> = arrayListOf()
            for (item in songList) {
                songListUris.add(item.audioUri)
            }
            return Triple(songList, songListUris, index)
        }
    }


    private fun setUpPlayerForeground(intent: Intent) {
        with(setUpData(intent))
        {
            audioPlayer.setUpPlayer(first, second, third)
            audioPlayer.setCommandControl { first[it].getMediaDescription() }


        }

    }


}

