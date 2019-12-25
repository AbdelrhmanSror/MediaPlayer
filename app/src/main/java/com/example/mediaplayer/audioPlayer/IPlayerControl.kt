package com.example.mediaplayer.audioPlayer

import android.net.Uri

interface IPlayerControl {

    /**
     * return current player index
     */
    fun currentIndex(): Int

    /**
     * return current player position in milliseconds
     */
    fun currentPosition(): Long

    /**
     * return current instance that refer to current playing audio
     * that is passed when calling [setUpPlayer] if nothing passed will return null
     */
    fun currentTag(): Any?


    fun setUpPlayer(audioList: List<Any>? = emptyList(), audioUris: List<Uri>, index: Int)

    /**
     * enable repeat mode
     */
    fun repeatModeEnable()

    /**
     * enable shuffle mode
     */
    fun shuffleModeEnable()

    /**
     * seek to different track
     */
    fun seekToIndex(index: Int)

    /**
     * seek to different position
     */
    fun seekToSecond(second: Int)

    /**
     * play audio and reset runnable callback of Audio progress if it was initialized before
     */
    fun play()

    /**
     * pause audio and remove runnable callback of Audio progress if it is initialized
     */
    fun pause()

    /**
     * go to next audio
     */
    fun next()

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    fun previous()

    /**
     * change the audio state from playing to pausing and vice verse
     */
    fun changeAudioState()


}