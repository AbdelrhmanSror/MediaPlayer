package com.example.mediaplayer.audioPlayer.notification

import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.model.SongModel
import javax.inject.Inject

class PlayerStateNotification @Inject constructor(private val player: AudioPlayer<SongModel>) : IPlayerState {

    override fun onPlay() {

    }

    override fun onPause() {
    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
    }

    override fun onDurationChange(duration: Long) {

    }
}