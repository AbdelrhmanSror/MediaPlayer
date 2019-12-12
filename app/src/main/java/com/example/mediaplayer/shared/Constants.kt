package com.example.mediaplayer.shared

import com.example.mediaplayer.BuildConfig


object PlayerActions {
    //action for opening service from activity or fragment.
    const val ACTION_FOREGROUND = BuildConfig.APPLICATION_ID + "foreground"
}

object PlayerDestinations {
    const val NOTIFICATION = BuildConfig.APPLICATION_ID + "notification"


}

const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11
const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"
