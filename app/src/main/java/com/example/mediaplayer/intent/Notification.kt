package com.example.mediaplayer.intent

import com.example.mediaplayer.BuildConfig

object NotificationAction {
    const val PLAY_PAUSE = "${BuildConfig.APPLICATION_ID}.PlayPause"
    const val NEXT = "${BuildConfig.APPLICATION_ID}.Next"
    const val PREVIOUS = "${BuildConfig.APPLICATION_ID}.Previous"
    const val STOP = "${BuildConfig.APPLICATION_ID}.Stop"
    const val NOTIFICATION = "${BuildConfig.APPLICATION_ID}.Notification"


}

const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11