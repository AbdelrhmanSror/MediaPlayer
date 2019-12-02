package com.example.mediaplayer


object PlayerActions {
    //action for opening service from activity or fragment.
    const val ACTION_FOREGROUND = BuildConfig.APPLICATION_ID + "foreground"
    const val PLAY_ACTION = BuildConfig.APPLICATION_ID + "play_notification"
    const val PAUSE_ACTION = BuildConfig.APPLICATION_ID + "pause_notification"
    const val PREVIOUS_ACTION = BuildConfig.APPLICATION_ID + "prev"
    const val NEXT_ACTION = BuildConfig.APPLICATION_ID + "next_notification"
    const val DELETE_ACTION = BuildConfig.APPLICATION_ID + "delete_notification"
}

object PlayerDestinations {
    const val NOTIFICATION=BuildConfig.APPLICATION_ID+"notification"


}

const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11
const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"
