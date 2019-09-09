package com.example.mediaplayer.foregroundService

import android.app.*
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
        val songFragmentIntent = Intent(context, ChosenSongActivity::class.java)
        songFragmentIntent.action = AUDIO_FOREGROUND_NOTIFICATION
        songFragmentIntent.putParcelableArrayListExtra(LIST_SONG, playListModels)
        songFragmentIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        val albumCoverImage = BitmapFactory.decodeFile(item.albumCoverUri)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                // the metadata for the currently playing track
                .setContentTitle(item.Title)
                .setContentText(item.actor)
                .setSubText(item.Title)
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
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

                .setLargeIcon(albumCoverImage).build()
    }

    private fun actionPause(): NotificationCompat.Action {
        val pauseIntent = Intent(context, ChosenSongService::class.java)
        pauseIntent.action = PAUSE_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_pause_black_24dp, context.getString(R.string.pause), notificationPendingIntent)
    }

    private fun actionPrevious(): NotificationCompat.Action {
        val prevIntent = Intent(context, ChosenSongService::class.java)
        prevIntent.action = PREVIOUS_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp, context.getString(R.string.previous), notificationPendingIntent)
    }

    private fun actionPlay(): NotificationCompat.Action {
        val playIntent = Intent(context, ChosenSongService::class.java)
        playIntent.action = PLAY_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp, context.getString(R.string.play), notificationPendingIntent)
    }

    private fun actionNext(): NotificationCompat.Action {
        val nextIntent = Intent(context, ChosenSongService::class.java)
        nextIntent.action = NEXT_ACTION
        val notificationPendingIntent = PendingIntent.getService(context,
                NOTIFICATION_ID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp, context.getString(R.string.next), notificationPendingIntent)
    }
}
