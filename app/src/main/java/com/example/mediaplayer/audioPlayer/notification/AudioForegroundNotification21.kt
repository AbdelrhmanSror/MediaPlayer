package com.example.mediaplayer.audioPlayer.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.graphics.Typeface
import android.support.v4.media.session.MediaSessionCompat
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mediaplayer.R
import com.example.mediaplayer.extensions.toUri
import com.example.mediaplayer.model.MusicNotificationModel
import com.example.mediaplayer.shared.CHANNEL_ID
import com.example.mediaplayer.shared.ImageLoader
import com.example.mediaplayer.shared.NOTIFICATION_ID
import kotlinx.coroutines.yield
import javax.inject.Inject

open class AudioForegroundNotification21 @Inject constructor(val service: Service
                                                             , private val mediaSession: MediaSessionCompat)
    : INotification {

    val notificationManager by lazy {
        service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private var isCreated = false

    var builder = NotificationCompat.Builder(service, CHANNEL_ID)

    init {
        create()

    }

    protected open fun extendInitialization() {}

    protected open fun startChronometer(bookmark: Long) {
    }

    protected open fun stopChronometer(bookmark: Long) {
    }

    private fun create() {
        if (isCreated) {
            return
        }
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)

                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(NotificationActions.stop(service))
        builder = NotificationCompat.Builder(service, CHANNEL_ID)
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add an app icon and set its accent color
                .setSmallIcon(R.drawable.vd_bird_not_singing)
                .setContentIntent(NotificationActions.contentIntentNotification(service, 0))
                .setDeleteIntent(NotificationActions.stop(service)
                )
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(mediaStyle)
                .setUsesChronometer(true)
                .addAction(NotificationActions.skipPrevious(service))
                .addAction(NotificationActions.playPause(service, false))
                .addAction(NotificationActions.skipNext(service))
                .setGroup("Hobo MUSIC")
                .setOnlyAlertOnce(true)
        isCreated = true
    }

    override suspend fun update(state: MusicNotificationModel): Notification {
        create()
        val title = state.title
        val artist = state.artist
        val album = state.album

        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, 0)
        // if(isPlaying)startChronometer()
        updateMetadataImpl(spannableTitle, artist, album)
        updateState(state.isPlaying)
        yield()
        val notification = builder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }

    private fun updateState(isPlaying: Boolean) {
        builder.mActions[1] = NotificationActions.playPause(service, isPlaying)
        builder.setSmallIcon(if (isPlaying) R.drawable.vd_bird_singing else R.drawable.vd_bird_not_singing)
        builder.setOngoing(isPlaying)

    }


    @Suppress("BlockingMethodInNonBlockingContext")
    protected open suspend fun updateMetadataImpl(
            title: SpannableString,
            artist: String,
            album: String?
    ) {
        builder.mActions[0] = NotificationActions.skipPrevious(service)
        builder.mActions[2] = NotificationActions.skipNext(service)
        Log.v("serviceDestroyed", "album uri ${album.toUri()}")

        builder.setLargeIcon(ImageLoader.getImageBitmap(service, album.toUri()))
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(album)
    }


    override fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

}