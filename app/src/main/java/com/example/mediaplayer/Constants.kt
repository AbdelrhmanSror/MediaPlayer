package com.example.mediaplayer


enum class PlayerActions(val value: String) {
    //action for opening service from activity or fragment.
    ACTION_FOREGROUND(BuildConfig.APPLICATION_ID + "foreground"),
    PLAY_ACTION("${BuildConfig.APPLICATION_ID}.play_notification"),
    PAUSE_ACTION("${BuildConfig.APPLICATION_ID}.pause_notification"),
    PREVIOUS_ACTION("${BuildConfig.APPLICATION_ID}.prev"),
    NEXT_ACTION("${BuildConfig.APPLICATION_ID}.next_notification"),
    DELETE_ACTION("${BuildConfig.APPLICATION_ID}.delete_notification"),
}

enum class PlayerDestinations(val value: String) {
    //action for detecting the destination so when notification is clicked we can setup navigation based on it
    // and also we can query the proper table in database
    PLAYLIST(BuildConfig.APPLICATION_ID + "playList"),
    FAVOURITE("${BuildConfig.APPLICATION_ID}.favourite"),
    TRACKS("${BuildConfig.APPLICATION_ID}.tracks")
    ,
    NOTIFICATION("${BuildConfig.APPLICATION_ID}.notification")


}

const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11
const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"
const val SERVICE_STATE = "serviceState"
const val PLAYER_ACTION = "playerAction"
