package com.example.mediaplayer.chosenSong

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mediaplayer.R
import com.example.mediaplayer.constants.*
import com.example.mediaplayer.playlist.PlayListModel
import java.util.*

class NotificationController(private val playListModels: ArrayList<PlayListModel>?
                             , private val context: Context) {

    private fun createNotificationChannel() {
        val notifManager = NotificationManagerCompat.from(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create a NotificationController
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    "Media Notification", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(null, null)
            notificationChannel.vibrationPattern = null
            notificationChannel.description = "MediaPlayer"
            notifManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     *
     * @param isPlaying variable indicates whether the current audio is playing or not
     * and based on it we chose to display play or pause drawable
     * @param chosenSongIndex indicates to the index of current playing song
     *
     * @return notification
     */
    internal fun buildNotification(isPlaying: Boolean, chosenSongIndex: Int): Notification {
        val playPauseAction: NotificationCompat.Action
        val item = playListModels!![chosenSongIndex]
        if (isPlaying) {
            playPauseAction = actionPause()
        } else {
            playPauseAction = actionPlay()
        }
        val songFragmentIntent = Intent(context, ChosenSongActivity::class.java)
        songFragmentIntent.action = AUDIO_FOREGROUND_NOTIFICATION
        songFragmentIntent.putParcelableArrayListExtra(LIST_SONG, playListModels)
        songFragmentIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        //creating notification channel necessary for android version 26 and above
        createNotificationChannel()
        val albumCoverImage = BitmapFactory.decodeFile(item.albumCoverUri)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                // the metadata for the currently playing track
                .setContentTitle(item.title)
                .setContentText(item.actor)
                .setSubText(item.title)
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                .setSmallIcon(R.drawable.exo_icon_play)
                .setColor(ContextCompat.getColor(context, R.color.blue))
                .setContentIntent(PendingIntent.getActivity(context,
                        NOTIFICATION_ID, songFragmentIntent, 0))
                //Add a previous button
                .addAction(actionPrevious()) // #0
                //Add a pause button
                .addAction(playPauseAction) // #1
                //Add a next button
                .addAction(actionNext())     // #2
                // Apply the media style template
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

                .setLargeIcon(albumCoverImage).build()
    }

    private fun actionPause(): NotificationCompat.Action {
        val pauseIntent = Intent(context, ChosenSongService::class.java)
        pauseIntent.action = PAUSE_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_pause, context.getString(R.string.pause), notificationPendingIntent)
    }

    private fun actionPrevious(): NotificationCompat.Action {
        val prevIntent = Intent(context, ChosenSongService::class.java)
        prevIntent.action = PREVIOUS_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_previous, context.getString(R.string.previous), notificationPendingIntent)
    }

    private fun actionPlay(): NotificationCompat.Action {
        val playIntent = Intent(context, ChosenSongService::class.java)
        playIntent.action = PLAY_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_play, context.getString(R.string.play), notificationPendingIntent)
    }

    private fun actionNext(): NotificationCompat.Action {
        val nextIntent = Intent(context, ChosenSongService::class.java)
        nextIntent.action = NEXT_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_play_next, context.getString(R.string.next), notificationPendingIntent)
    }
}
