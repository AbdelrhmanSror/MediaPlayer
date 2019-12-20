package com.example.mediaplayer.audioPlayer.notification

import android.app.Notification
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.model.Event
import com.example.mediaplayer.model.MusicNotificationModel
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.shared.CustomScope
import com.example.mediaplayer.shared.NOTIFICATION_ID
import com.example.mediaplayer.shared.isOreo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioForegroundNotificationManager @Inject constructor(private val service: AudioForegroundService,
                                                             private val player: AudioPlayer,
                                                             private val notificationImp: INotification) :
        DefaultLifecycleObserver, CoroutineScope by CustomScope() {

    private var isForeground: Boolean = false

    private var notificationJob: Job? = null
    private val publisher = Channel<Event>(Channel.UNLIMITED)
    private val currentState = MusicNotificationModel()
    private var isReceiverRegistered = false


    private val playerListener = object : IPlayerState {

        override fun onPlay() {
            onNextState(true)
        }

        override fun onPause() {
            onNextState(false)
        }

        override fun onAudioChanged(index: Int, isPlaying: Boolean, currentInstance: Any?) {
            currentInstance?.let {
                onNextMetadata(currentInstance as SongModel)

            }
        }

    }

    /* fun registerReceiver() {
         if (!isReceiverRegistered) {
             Log.v("registeringAudioSession", " done reg ")

             isReceiverRegistered = true
             val intentFilter = IntentFilter(NOTIFICATION_EVENT)
             intentFilter.priority=100
             service.registerReceiver(NotificationIntentReceiver(), intentFilter)
         }
     }

     fun unregisterReciever() {
         isReceiverRegistered = false
         service.unregisterReceiver(NotificationIntentReceiver())

     }*/

    companion object {
        private const val METADATA_PUBLISH_DELAY = 350L
        private const val STATE_PUBLISH_DELAY = 100L
    }

    init {
        player.registerObserver(playerListener)
        launch {
            publisher.consumeAsFlow()
                    .filter { event ->
                        when (event) {
                            is Event.Metadata -> currentState.isDifferentMetadata(event.entity)
                            is Event.State -> currentState.isDifferentState(event.state)
                        }
                    }.collect { consumeEvent(it) }
        }

    }

    private suspend fun consumeEvent(event: Event) {
        notificationJob?.cancel()
        when (event) {
            is Event.Metadata -> {
                if (currentState.updateMetadata(event.entity)) {
                    publishNotification(currentState.deepCopy(), METADATA_PUBLISH_DELAY)
                }
            }
            is Event.State -> {
                if (currentState.updateState(event.state)) {
                    publishNotification(currentState.deepCopy(), STATE_PUBLISH_DELAY)
                }
            }
        }
    }

    private suspend fun publishNotification(state: MusicNotificationModel, delay: Long) {
        //require(state !== state) // to avoid concurrency problems a copy is passed

        if (!isForeground && isOreo()) {
            // oreo needs to post notification immediately after calling startForegroundService
            issueNotification(state)
        } else {
            // post delayed
            notificationJob = GlobalScope.launch {
                kotlinx.coroutines.delay(delay)
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


    override fun onDestroy(owner: LifecycleOwner) {
        stopForeground()
        notificationJob?.cancel()
    }

    private fun onNextMetadata(metadata: SongModel) {
        publisher.offer(Event.Metadata(metadata))
    }

    private fun onNextState(state: Boolean) {
        publisher.offer(Event.State(state))
    }

    private fun stopForeground() {
        if (!isForeground) {
            return
        }
        service.stopForeground(true)
        notificationImp.cancel()
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