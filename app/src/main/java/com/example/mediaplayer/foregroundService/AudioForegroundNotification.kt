package com.example.mediaplayer.foregroundService

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.mediaplayer.*
import com.example.mediaplayer.model.SongModel
import java.util.*


class AudioForegroundNotification(private val songModels: ArrayList<SongModel>?
                                  , private val context: Context) {
    private val customCollapsedNotification = RemoteViews(context.packageName, R.layout.custom_audio_notification_collapsed).apply {
        setImageViewResource(R.id.notification_prev, R.drawable.previous_collapsed_notification)
        setImageViewResource(R.id.notification_next, R.drawable.next_collapsed_notification)
        setOnClickPendingIntent(R.id.notification_next, pendingIntentNext())
        setOnClickPendingIntent(R.id.notification_prev, pendingIntentPrevious())

    }
    private val customExpandedNotification = RemoteViews(context.packageName, R.layout.custom_audio_notification_expanded).apply {
        setImageViewResource(R.id.notification_prev, R.drawable.previous__expanded_notification)
        setImageViewResource(R.id.notification_next, R.drawable.next__expanded_notification)
        setImageViewResource(R.id.notification_close, R.drawable.close__expanded_notification)
        setOnClickPendingIntent(R.id.notification_next, pendingIntentNext())
        setOnClickPendingIntent(R.id.notification_prev, pendingIntentPrevious())
        setOnClickPendingIntent(R.id.notification_close,pendingIntentDelete())

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
        val item = songModels!![chosenSongIndex]
        customCollapsedNotification.apply {
            setTextViewText(R.id.notification_title, item.title)
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
            setTextViewText(R.id.notification_title, item.title)
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
                .setSubText(item.title)
                .setCustomContentView(customCollapsedNotification)
                .setCustomBigContentView(customExpandedNotification)
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                .setSmallIcon(R.drawable.play_collapsed_notification)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))

                .setContentIntent(onNotificationClickedIntent(chosenSongIndex))
                // Add a cancel button
                .setStyle(androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle()
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(pendingIntentDelete()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setDeleteIntent(pendingIntentDelete())
                .setLargeIcon(albumCoverImage)
                .setOnlyAlertOnce(true)
                .build()
    }

    private fun onNotificationClickedIntent(chosenSongIndex: Int): PendingIntent {
        val bundle = Bundle()
        //bundle.putParcelableArrayList(LIST_SONG, songModels)
        bundle.putInt(CHOSEN_SONG_INDEX, chosenSongIndex)
        bundle.putBoolean(PlayerDestinations.NOTIFICATION, true)
        //using deep links to navigate to chosen song fragment
        return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigaion)
                .setDestination(R.id.chosenSong_dest)
                .setArguments(bundle)
                .createPendingIntent()
    }

    private fun pendingIntentPause(): PendingIntent {
        val pauseIntent = Intent(context, AudioForegroundService::class.java)
        pauseIntent.action = PlayerActions.PAUSE_ACTION
        return PendingIntent.getService(context,
                NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pendingIntentDelete(): PendingIntent {
        val deleteIntent = Intent(context, AudioForegroundService::class.java)
        deleteIntent.action = PlayerActions.DELETE_ACTION
        return PendingIntent.getService(context,
                NOTIFICATION_ID, deleteIntent, 0)
    }

    private fun pendingIntentPrevious(): PendingIntent {
        val prevIntent = Intent(context, AudioForegroundService::class.java)
        prevIntent.action = PlayerActions.PREVIOUS_ACTION
        return PendingIntent.getService(context,
                NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pendingIntentPlay(): PendingIntent {
        val playIntent = Intent(context, AudioForegroundService::class.java)
        playIntent.action = PlayerActions.PLAY_ACTION
        return PendingIntent.getService(context,
                NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun pendingIntentNext(): PendingIntent {
        val nextIntent = Intent(context, AudioForegroundService::class.java)
        nextIntent.action = PlayerActions.NEXT_ACTION
        return PendingIntent.getService(context,
                NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}