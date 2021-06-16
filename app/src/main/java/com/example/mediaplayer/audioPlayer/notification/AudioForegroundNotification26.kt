/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.audioPlayer.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.intent.CHANNEL_ID

@RequiresApi(Build.VERSION_CODES.O)
internal class AudioForegroundNotification26 constructor(
        service: Service,
        mediaSession: MediaSessionCompat

) : AudioForegroundNotification24(service, mediaSession) {

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