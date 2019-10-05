package com.example.mediaplayer.foregroundService

import android.app.*
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mediaplayer.*
import com.example.mediaplayer.model.PlayListModel
import java.util.*

class NotificationChannel : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notifyManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a ForegroundNotification
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    "Media Notification", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)

            }
            notificationChannel.description = "MediaPlayer"
            notifyManager.createNotificationChannel(notificationChannel)
        }
    }
}

class ForegroundNotification(private val playListModels: ArrayList<PlayListModel>?
                             , private val context: Context) {
    private val customCollapsedNotification = RemoteViews(context.packageName, R.layout.custom_audio_notification_collapsed).apply {
        setImageViewResource(R.id.notification_prev, R.drawable.previous_collapsed_notification)
        setImageViewResource(R.id.notification_next, R.drawable.next_collapsed_notification)
        setImageViewResource(R.id.notification_close, R.drawable.close_collapsed_notification)
        setOnClickPendingIntent(R.id.notification_next, pendingIntentNext())
        setOnClickPendingIntent(R.id.notification_prev, pendingIntentPrevious())
        setOnClickPendingIntent(R.id.notification_close, pendingIntentDelete())

    }
    private val customExpandedNotification = RemoteViews(context.packageName, R.layout.custom_audio_notification_expanded).apply {
        setImageViewResource(R.id.notification_prev, R.drawable.previous__expanded_notification)
        setImageViewResource(R.id.notification_next, R.drawable.next__expanded_notification)
        setImageViewResource(R.id.notification_close, R.drawable.close__expanded_notification)
        setOnClickPendingIntent(R.id.notification_next, pendingIntentNext())
        setOnClickPendingIntent(R.id.notification_prev, pendingIntentPrevious())
        setOnClickPendingIntent(R.id.notification_close, pendingIntentDelete())

    }


    /**
     *
     * @param isPlaying variable indicates whether the current audio is playing or not
     * and based on it we chose to display play_collapsed_notification or pause_collapsed_notification drawable
     * @param chosenSongIndex indicates to the index of current playing song
     *
     * @return notification
     */
    fun build(isPlaying: Boolean, chosenSongIndex: Int): Notification {
        val item = playListModels!![chosenSongIndex]
        customCollapsedNotification.apply {
            setTextViewText(R.id.notification_title, item.Title)
            setTextViewText(R.id.notification_subtitle, item.actor)
            when (isPlaying) {
                true -> {
                    setImageViewResource(R.id.notification_play_pause_button, R.drawable.pause_collapsed_notification)
                    setOnClickPendingIntent(R.id.notification_play_pause_button, pendingIntentPause())
                }
                else -> {
                    setImageViewResource(R.id.notification_play_pause_button, R.drawable.play_collapsed_notification)
                    setOnClickPendingIntent(R.id.notification_play_pause_button, pendingIntentPlay())
                }
            }
        }

        customExpandedNotification.apply {
            setTextViewText(R.id.notification_title, item.Title)
            setTextViewText(R.id.notification_subtitle, item.actor)
            when (isPlaying) {
                true -> {
                    setImageViewResource(R.id.notification_play_pause_button, R.drawable.pause__expanded_notification)
                    setOnClickPendingIntent(R.id.notification_play_pause_button, pendingIntentPause())
                }
                else -> {
                    setImageViewResource(R.id.notification_play_pause_button, R.drawable.play__expanded_notification)
                    setOnClickPendingIntent(R.id.notification_play_pause_button, pendingIntentPlay())
                }
            }
        }
        val albumCoverImage = BitmapFactory.decodeFile(item.albumCoverUri)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.default_image)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                // the metadata for the currently playing track
                .setSubText(item.Title)
                .setCustomContentView(customCollapsedNotification)
                .setCustomBigContentView(customExpandedNotification)
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                .setSmallIcon(R.drawable.play_collapsed_notification)
                .setColor(ContextCompat.getColor(context, R.color.blue))
                .setContentIntent(PendingIntent.getActivity(context,
                        NOTIFICATION_ID, notificationClickedIntent(chosenSongIndex), 0))
                .setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setDeleteIntent(pendingIntentDelete())
                .setLargeIcon(albumCoverImage)
                .build()
    }

    private fun notificationClickedIntent(chosenSongIndex: Int): Intent {
        val intent = Intent(context, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelableArrayList(LIST_SONG, playListModels)
        bundle.putInt(CHOSEN_SONG_INDEX, chosenSongIndex)
        bundle.putString(FRAGMENT_PURPOSE, PlayerActions.AUDIO_FOREGROUND_NOTIFICATION.value)
        intent.putExtras(bundle)
        return intent
    }

    private fun pendingIntentPause(): PendingIntent {
        val pauseIntent = Intent(context, AudioForgregroundService::class.java)
        pauseIntent.action = PlayerActions.PAUSE_ACTION.value
        return PendingIntent.getService(context,
                NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pendingIntentDelete(): PendingIntent {
        val deleteIntent = Intent(context, AudioForgregroundService::class.java)
        deleteIntent.action = PlayerActions.DELETE_ACTION.value
        return PendingIntent.getService(context,
                NOTIFICATION_ID, deleteIntent, 0)
    }

    private fun pendingIntentPrevious(): PendingIntent {
        val prevIntent = Intent(context, AudioForgregroundService::class.java)
        prevIntent.action = PlayerActions.PREVIOUS_ACTION.value
        return PendingIntent.getService(context,
                NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pendingIntentPlay(): PendingIntent {
        val playIntent = Intent(context, AudioForgregroundService::class.java)
        playIntent.action = PlayerActions.PLAY_ACTION.value
        return PendingIntent.getService(context,
                NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pendingIntentNext(): PendingIntent {
        val nextIntent = Intent(context, AudioForgregroundService::class.java)
        nextIntent.action = PlayerActions.NEXT_ACTION.value
        return PendingIntent.getService(context,
                NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
