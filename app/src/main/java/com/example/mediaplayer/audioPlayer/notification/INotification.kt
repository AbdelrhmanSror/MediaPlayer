package com.example.mediaplayer.audioPlayer.notification

import android.app.Notification
import com.example.mediaplayer.model.SongModel


interface INotification {
    suspend fun update(songModel: SongModel, isPlaying: Boolean): Notification
    fun cancel()

}