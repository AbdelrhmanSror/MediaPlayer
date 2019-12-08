package com.example.mediaplayer.audioPlayer.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.CHANNEL_ID
import com.example.mediaplayer.audioPlayer.AudioPlayer
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
internal class AudioForegroundNotification26 @Inject constructor(
        service: Service, audioPlayer: AudioPlayer,
        mediaSession: MediaSessionCompat

) : AudioForegroundNotification24(service, audioPlayer, mediaSession) {

    override fun extendInitialization() {
        builder.setColorized(true)

        val nowPlayingChannelExists = notificationManager.getNotificationChannel(CHANNEL_ID) != null

        if (!nowPlayingChannelExists) {
            createNotificationChannel()
        }
    }

    //creating notification channel
    private fun createNotificationChannel() {
        val notifyManager = NotificationManagerCompat.from(service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a ForegroundNotification
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    "Media Notification", NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = "MediaPlayer"
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            notificationChannel.setShowBadge(false)
            notifyManager.createNotificationChannel(notificationChannel)
        }
    }

}