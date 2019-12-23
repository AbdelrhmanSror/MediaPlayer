package com.example.mediaplayer.audioPlayer.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.mediaplayer.R
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.intent.CHOSEN_SONG_INDEX
import com.example.mediaplayer.intent.NOTIFICATION_ID
import com.example.mediaplayer.intent.NotificationAction
import com.example.mediaplayer.intent.NotificationAction.NOTIFICATION


object NotificationActions {
    fun playPause(context: Context, isPlaying: Boolean): NotificationCompat.Action {
        val icon = if (isPlaying) R.drawable.vd_pause_big else R.drawable.vd_play_big
        return NotificationCompat.Action.Builder(
                icon,
                "Toggle play pause",
                buildMediaPendingIntent(context, NotificationAction.PLAY_PAUSE)
        ).build()
    }


    fun skipPrevious(context: Context): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
                R.drawable.vd_skip_previous,
                "Skip to previous",
                buildMediaPendingIntent(context, NotificationAction.PREVIOUS)
        ).build()
    }


    fun skipNext(context: Context): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
                R.drawable.vd_skip_next,
                "Skip to next",
                buildMediaPendingIntent(context, NotificationAction.NEXT)
        ).build()
    }

    fun stop(context: Context): PendingIntent {
        return buildMediaPendingIntent(
                context,
                NotificationAction.STOP
        )
    }

    fun contentIntentNotification(context: Context, index: Int): PendingIntent {
        val bundle = Bundle()
        //bundle.putParcelableArrayList(LIST_SONG, songModels)
        bundle.putInt(CHOSEN_SONG_INDEX, index)
        bundle.putBoolean(NOTIFICATION, true)
        //using deep links to navigate to chosen song fragment
        return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigaion)
                .setDestination(R.id.chosenSong_dest)
                .setArguments(bundle)
                .createPendingIntent()
    }

    private fun buildMediaPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, AudioForegroundService::class.java)
        intent.action = action
        return PendingIntent.getService(context,
                NOTIFICATION_ID, intent, 0)
    }

}