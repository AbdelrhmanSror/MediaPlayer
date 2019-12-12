package com.example.mediaplayer.audioPlayer.notification

import android.app.Service
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.example.mediaplayer.R
import com.example.mediaplayer.shared.twoDigitNumber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
internal open class AudioForegroundNotification24 @Inject constructor(
        service: Service,
        mediaSession: MediaSessionCompat

) : AudioForegroundNotification(service, mediaSession) {

    override fun startChronometer(bookmark: Long) {
        builder.setWhen(System.currentTimeMillis() - bookmark)
                .setShowWhen(true)
                .setUsesChronometer(true)
        builder.setSubText(null)
    }

    override fun stopChronometer(bookmark: Long) {
        builder.setWhen(0)
                .setShowWhen(false)
                .setUsesChronometer(false)
        val min = (bookmark / 60).toInt().twoDigitNumber()
        val sec = (bookmark % 60).toInt().twoDigitNumber()
        val duration = service.getString(R.string.duration_format, min, sec)
        builder.setSubText(duration)
    }


}