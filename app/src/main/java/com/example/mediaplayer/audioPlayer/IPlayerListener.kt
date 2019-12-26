package com.example.mediaplayer.audioPlayer


interface IPlayerListener {
    /**
     * this will be called when there is audio playing
     *
     */
    fun onActivePlayer() {}

    /**
     * this will be called when audio paused
     */
    fun onInActivePlayer() {}

    /**
     * will be called when the corresponding observer is remove from list of observers
     */
    fun onObserverDetach(iPlayerObserver: IPlayerObserver) {}


}