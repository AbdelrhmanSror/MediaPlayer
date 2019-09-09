package com.example.mediaplayer

//action for opening service from activity or fragment.
const val ACTION_FOREGROUND = BuildConfig.APPLICATION_ID + "foreground"
//action for opening ChosenSongFragment with foreground  service
const val ACTION_PLAYING_AUDIO_FOREGROUND = BuildConfig.APPLICATION_ID + "playAudio"
//for actions coming from clicking on notification to open ChosenSongFragment
const val AUDIO_FOREGROUND_NOTIFICATION = BuildConfig.APPLICATION_ID + "playAudioNotification"

const val PLAY_ACTION = BuildConfig.APPLICATION_ID + ".play"
const val PAUSE_ACTION = BuildConfig.APPLICATION_ID + ".pause"
const val PREVIOUS_ACTION = BuildConfig.APPLICATION_ID + ".prev"
const val NEXT_ACTION = BuildConfig.APPLICATION_ID + ".next"
const val CHANNEL_ID = "5"
const val NOTIFICATION_ID = 11
const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"