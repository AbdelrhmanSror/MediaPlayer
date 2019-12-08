package com.example.mediaplayer.audioPlayer.notification

import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.IpLayerState
import javax.inject.Inject

class PlayerStateNotification @Inject constructor(private val player: AudioPlayer) : IpLayerState {

    override fun onPlay() {

    }

    override fun onPause() {
    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
    }

    override fun onDurationChange(duration: Long) {

    }
}