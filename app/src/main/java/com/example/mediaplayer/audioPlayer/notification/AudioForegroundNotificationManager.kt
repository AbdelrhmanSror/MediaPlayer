package com.example.mediaplayer.audioPlayer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.intent.CHANNEL_ID
import com.example.mediaplayer.intent.NOTIFICATION_ID
import com.example.mediaplayer.model.Event
import com.example.mediaplayer.model.MusicNotificationModel
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.shared.CustomScope
import com.example.mediaplayer.shared.isOreo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioForegroundNotificationManager @Inject constructor(private val service: AudioForegroundService,
                                                             private val notificationImp: INotification) : IPlayerState,
        DefaultLifecycleObserver, CoroutineScope by CustomScope() {

    private var isForeground: Boolean = false

    private var notificationJob: Job? = null
    private val currentState = MusicNotificationModel()
    private val onDataChanged = MutableLiveData<Event>()
    private val observer = Observer<Event> {
        when (it) {
            is Event.Metadata -> {
                if (currentState.isDifferentMetadata(it.entity))
                    launch { consumeEvent(it) }
            }
            is Event.State -> {
                if (currentState.isDifferentState(it.state))
                    launch { consumeEvent(it) }
            }
        }
    }


    override fun onPlay() {
        onNextState(true)
    }

    override fun onPause() {
        onNextState(false)
    }


    override fun onAudioChanged(index: Int, currentInstance: Any?) {
        currentInstance?.let {
            onNextMetadata(currentInstance as SongModel)

        }
    }


    override fun onDestroy(owner: LifecycleOwner) {
        onDataChanged.removeObserver(observer)
        stopForeground()
        notificationJob?.cancel()
    }

    init {
        createNotificationChannel()
        service.lifecycle.addObserver(this)
        onDataChanged.observeForever(observer)
    }

    //creating notification channel
    private fun createNotificationChannel() {
        val notifyManager = NotificationManagerCompat.from(service)
        if (isOreo()) {
            // Create a ForegroundNotification
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    "Media Notification", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)


            }
            notificationChannel.description = "MediaPlayer"
            notifyManager.createNotificationChannel(notificationChannel)
        }
    }

    private suspend fun consumeEvent(event: Event) {
        notificationJob?.cancel()
        when (event) {
            is Event.Metadata -> {
                if (currentState.updateMetadata(event.entity)) {
                    publishNotification(currentState.deepCopy())
                }
            }
            is Event.State -> {
                if (currentState.updateState(event.state)) {
                    publishNotification(currentState.deepCopy())
                }
            }
        }
    }

    private suspend fun publishNotification(state: MusicNotificationModel) {
        //require(state !== state) // to avoid concurrency problems a copy is passed

        if (!isForeground && isOreo()) {
            // oreo needs to post notification immediately after calling startForegroundService
            issueNotification(state)
        } else {
            notificationJob = GlobalScope.launch {
                issueNotification(state)
            }
        }
    }

    private suspend fun issueNotification(state: MusicNotificationModel) {
        val notification = notificationImp.update(state)
        if (state.isPlaying) {
            startForeground(notification)
        } else {
            pauseForeground()
        }
    }


    private fun onNextMetadata(metadata: SongModel) {
        onDataChanged.value = Event.Metadata(metadata)
    }

    private fun onNextState(state: Boolean) {
        onDataChanged.value = Event.State(state)
    }

    private fun stopForeground() {
        if (!isForeground) {
            return
        }
        notificationImp.cancel()
        service.stopForeground(true)
        isForeground = false
    }

    private fun pauseForeground() {
        if (!isForeground) {
            return
        }
        // state paused
        service.stopForeground(false)

        isForeground = false
    }

    private fun startForeground(notification: Notification) {
        if (isForeground) {
            return
        }
        service.startForeground(NOTIFICATION_ID, notification)

        isForeground = true
    }
}