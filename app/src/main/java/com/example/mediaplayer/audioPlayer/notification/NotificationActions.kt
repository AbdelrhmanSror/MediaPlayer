package com.example.mediaplayer.audioPlayer.notification

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import androidx.navigation.NavDeepLinkBuilder
import com.example.mediaplayer.R
import com.example.mediaplayer.shared.CHOSEN_SONG_INDEX
import com.example.mediaplayer.shared.PlayerDestinations

object NotificationActions {


    fun playPause(context: Context, isPlaying: Boolean): NotificationCompat.Action {
        val icon = if (isPlaying) R.drawable.vd_pause_big else R.drawable.vd_play_big
        return NotificationCompat.Action.Builder(
                icon,
                "Toggle play pause",
                buildMediaPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE)
        ).build()
    }


    fun skipPrevious(context: Context): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
                R.drawable.vd_skip_previous,
                "Skip to previous",
                buildMediaPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        ).build()
    }


    fun skipNext(context: Context): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
                R.drawable.vd_skip_next,
                "Skip to next",
                buildMediaPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        ).build()
    }

    fun stop(context: Context): PendingIntent {
        return buildMediaPendingIntent(
                context,
                PlaybackStateCompat.ACTION_STOP
        )
    }

    fun contentIntentNotification(context: Context, index: Int): PendingIntent {
        val bundle = Bundle()
        //bundle.putParcelableArrayList(LIST_SONG, songModels)
        bundle.putInt(CHOSEN_SONG_INDEX, index)
        bundle.putBoolean(PlayerDestinations.NOTIFICATION, true)
        //using deep links to navigate to chosen song fragment
        return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigaion)
                .setDestination(R.id.chosenSong_dest)
                .setArguments(bundle)
                .createPendingIntent()
    }

    private fun buildMediaPendingIntent(context: Context, action: Long): PendingIntent {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                ComponentName(context, NotificationIntentReceiver::class.java),
                action
        )
    }

}