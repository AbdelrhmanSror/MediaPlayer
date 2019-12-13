package com.example.mediaplayer.audioPlayer.notification

import android.app.Notification
import com.example.mediaplayer.model.MusicNotificationModel


interface INotification {
    suspend fun update(state: MusicNotificationModel): Notification
    fun cancel()

}