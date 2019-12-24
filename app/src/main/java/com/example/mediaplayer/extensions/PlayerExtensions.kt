package com.example.mediaplayer.extensions

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

fun Player.isPlaying() = playWhenReady

fun Player.isPlayerStopping() = ExoPlayer.STATE_IDLE == playbackState

fun Player.isPlayerStateReady() = playbackState == ExoPlayer.STATE_READY

fun Player.isPlayerStateEnded() = playbackState == ExoPlayer.STATE_ENDED

fun Player.isNewDurationReady(durationHandled: Boolean) = isPlayerStateReady() && !durationHandled

fun Player.isPlayerPausing() = !playWhenReady && isPlayerStateReady()

fun Player.isPlayerPlaying() = playWhenReady && isPlayerStateReady()

fun Player.isTracksEnded(trackEndHandled: Boolean) = isPlayerStateEnded() && !trackEndHandled

fun Player.isTrackChanged(currentAudioIndex: Int, reason: Int): Boolean {
    return reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason == Player.DISCONTINUITY_REASON_SEEK && currentWindowIndex != currentAudioIndex
}