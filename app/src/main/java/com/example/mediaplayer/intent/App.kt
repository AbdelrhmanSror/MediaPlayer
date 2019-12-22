package com.example.mediaplayer.intent

import com.example.mediaplayer.BuildConfig


object PlayerActions {
    //action for opening service from activity or fragment.
    const val ACTION_FOREGROUND = BuildConfig.APPLICATION_ID + "foreground"
}



const val LIST_SONG = "chosenSong"
const val CHOSEN_SONG_INDEX = "chosenSongIndex"
