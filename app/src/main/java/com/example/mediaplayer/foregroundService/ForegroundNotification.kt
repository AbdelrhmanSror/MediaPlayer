package com.example.mediaplayer.foregroundService

import android.app.*
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mediaplayer.*
import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.ui.chosenSong.ChosenSongActivity
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
                    "Media Notification", NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = "MediaPlayer"
            notifyManager.createNotificationChannel(notificationChannel)
        }
    }
}


class ForegroundNotification(private val playListModels: ArrayList<PlayListModel>?
                             , private val context: Context) {
    /**
     *
     * @param isPlaying variable indicates whether the current audio is playing or not
     * and based on it we chose to display play or pause drawable
     * @param chosenSongIndex indicates to the index of current playing song
     *
     * @return notification
     */
    fun build(isPlaying: Boolean, chosenSongIndex: Int): Notification {
        val playPauseAction: NotificationCompat.Action = if (isPlaying) {
            actionPause()
        } else {
            actionPlay()
        }
        val item = playListModels!![chosenSongIndex]
        val albumCoverImage = BitmapFactory.decodeFile(item.albumCoverUri)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                // the metadata for the currently playing track
                .setContentTitle(item.Title)
                .setContentText(item.actor)
                .setSubText(item.Title)
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                .setSmallIcon(R.drawable.play)
                .setColor(ContextCompat.getColor(context, R.color.blue))
                .setContentIntent(PendingIntent.getActivity(context,
                        NOTIFICATION_ID, notificationClickedIntent(chosenSongIndex), 0))
                //Add a previous button
                .addAction(actionPrevious()) // #0
                //Add a pause button
                .addAction(playPauseAction) // #1
                //Add a next button
                .addAction(actionNext())     // #2
                // Apply the media style template
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        /** cancel image button will show only on devices prior to lollipop*/
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(actionDelete())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setDeleteIntent(actionDelete())
                .setLargeIcon(albumCoverImage).build()
    }

    private fun notificationClickedIntent(chosenSongIndex: Int): Intent {
        val intent = Intent(context, ChosenSongActivity::class.java)
        intent.action = PlayerActions.AUDIO_FOREGROUND_NOTIFICATION.value
        intent.putParcelableArrayListExtra(LIST_SONG, playListModels)
        intent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)

        return intent
    }
    private fun actionPause(): NotificationCompat.Action {
        val pauseIntent = Intent(context, ChosenSongService::class.java)
        pauseIntent.action = PlayerActions.PAUSE_ACTION.value
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.pause, context.getString(R.string.pause), notificationPendingIntent)
    }

    private fun actionDelete(): PendingIntent {
        val deleteIntent = Intent(context, ChosenSongService::class.java)
        deleteIntent.action = PlayerActions.DELETE_ACTION.value
        return PendingIntent.getService(context,
                NOTIFICATION_ID, deleteIntent, 0)
    }

    private fun actionPrevious(): NotificationCompat.Action {
        val prevIntent = Intent(context, ChosenSongService::class.java)
        prevIntent.action = PlayerActions.PREVIOUS_ACTION.value
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.previous, context.getString(R.string.previous), notificationPendingIntent)
    }

    private fun actionPlay(): NotificationCompat.Action {
        val playIntent = Intent(context, ChosenSongService::class.java)
        playIntent.action = PlayerActions.PLAY_ACTION.value
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.play, context.getString(R.string.play), notificationPendingIntent)
    }

    private fun actionNext(): NotificationCompat.Action {
        val nextIntent = Intent(context, ChosenSongService::class.java)
        nextIntent.action = PlayerActions.NEXT_ACTION.value
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.next, context.getString(R.string.next), notificationPendingIntent)
    }
}
