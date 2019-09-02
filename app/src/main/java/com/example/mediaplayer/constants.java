package com.example.mediaplayer;

public class constants {
    //action for opening service from activity or fragment.
    public static final String ACTION_FOREGROUND= BuildConfig.APPLICATION_ID+"foreground";
    //action for opening ChosenSongFragment from service
    public static final String ACTION_PLAYING_AUDIO_FOREGROUND= BuildConfig.APPLICATION_ID+"playAudio";
    //for actions coming from clicking on notification to open ChosenSongFragment
    public static final String AUDIO_FOREGROUND_NOTIFICATION= BuildConfig.APPLICATION_ID+"playAudioNotification";

    public static final String PLAY_ACTION=BuildConfig.APPLICATION_ID+".play";
    public static final String PAUSE_ACTION=BuildConfig.APPLICATION_ID+".pause";
    public static final String PREVIOUS_ACTION=BuildConfig.APPLICATION_ID+".prev";
    public static final String NEXT_ACTION=BuildConfig.APPLICATION_ID+".next";
    public static final String CHANNEL_ID = "5";
    public static final int NOTIFICATION_ID = 11;
    public static final String  LIST_SONG="chosenSong";
    public static final String  CHOSEN_SONG_INDEX="chosenSongIndex";


}
