package com.example.mediaplayer.audioPlayer

import com.example.mediaplayer.model.SongModel

interface IPlayerControl {

    fun startPlayer(audioList: ArrayList<SongModel>, chosenAudioIndex: Int)

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
    fun seekTo(index: Int)

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
    fun next(dispatchEvent: Boolean)

    /**
     * go to previous audio
     * if the current audio did not exceed the 3 second
     * and user pressed on previous button then we reset the player to the beginning
     */
    fun previous(dispatchEvent: Boolean)

    /**
     * change the audio state from playing to pausing and vice verse
     */
    fun changeAudioState(dispatchEvent: Boolean)


    fun requestFocus()


}