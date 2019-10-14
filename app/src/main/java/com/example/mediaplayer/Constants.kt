package com.example.mediaplayer


enum class PlayerActions(val value: String) {

    //action for opening service from activity or fragment.
    ACTION_FOREGROUND(BuildConfig.APPLICATION_ID + "foreground"),
    //for actions coming from clicking on notification to open ChosenSongFragment
    AUDIO_FOREGROUND_NOTIFICATION(BuildConfig.APPLICATION_ID + "playAudioNotification"),
    //action for starting service from favourite playlist
    AUDIO_FOREGROUND_FAVOURITE(BuildConfig.APPLICATION_ID + "favouriteForeground"),

    PLAY_ACTION("${BuildConfig.APPLICATION_ID}.play_notification"),
    PAUSE_ACTION("${BuildConfig.APPLICATION_ID}.pause_notification"),
    PREVIOUS_ACTION("${BuildConfig.APPLICATION_ID}.prev"),
    NEXT_ACTION("${BuildConfig.APPLICATION_ID}.next_notification"),
    DELETE_ACTION("${BuildConfig.APPLICATION_ID}.delete_notification"),

}

const val FRAGMENT_PURPOSE = "fragment_purpose"
const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11
const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"
