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
    fun onObserverDetach(iPlayerState: IPlayerState) {}

    /**
     * this is called when the player  is being stopped
     * will  be called immediately if stopped through google assistant
     * because we actually do not stop the player when receiving stop intent (if the ui visible) from notification for sake of re preparing player again
     */
    fun onPlayerStop() {}
}