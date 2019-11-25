package com.example.mediaplayer


object PlayerActions {
    const val PLAYER_ACTION = "playerAction"
    //action for opening service from activity or fragment.
    const val ACTION_FOREGROUND = BuildConfig.APPLICATION_ID + "foreground"
    const val PLAY_ACTION = BuildConfig.APPLICATION_ID + "play_notification"
    const val PAUSE_ACTION = BuildConfig.APPLICATION_ID + "pause_notification"
    const val PREVIOUS_ACTION = BuildConfig.APPLICATION_ID + "prev"
    const val NEXT_ACTION = BuildConfig.APPLICATION_ID + "next_notification"
    const val DELETE_ACTION = BuildConfig.APPLICATION_ID + "delete_notification"
}

object PlayerDestinations {
    //action for detecting the destination so when notification is clicked we can setup navigation based on it
    // and also we can query the proper table in database
    const val PLAYLIST=BuildConfig.APPLICATION_ID + "playList"
    const val FAVOURITE=BuildConfig.APPLICATION_ID +"favourite"
    const val TRACKS=BuildConfig.APPLICATION_ID+"tracks"
    const val NOTIFICATION=BuildConfig.APPLICATION_ID+"notification"


}

const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11
const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"
