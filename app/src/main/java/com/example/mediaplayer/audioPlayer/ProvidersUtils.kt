/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.audioPlayer

import android.support.v4.media.session.MediaSessionCompat
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.audioPlayer.audioFocus.FocusRequestImp
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocus
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompat
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusPre
import com.example.mediaplayer.audioPlayer.notification.AudioForegroundNotification21
import com.example.mediaplayer.audioPlayer.notification.AudioForegroundNotificationManager
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.shared.isOreoOrLater
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

fun provideMediaSessionCompat(service: AudioForegroundService) = MediaSessionCompat(service, service.packageName)
fun provideMediaSessionConnectorAdapter(service: AudioForegroundService, mediaSessionCompat: MediaSessionCompat) = MediaSessionConnectorAdapter(mediaSessionCompat, MediaSessionConnector(MediaSessionCompat(service, service.packageName)))
fun provideSimpleExoPlayer(service: AudioForegroundService) = ExoPlayerFactory.newSimpleInstance(service)
fun provideMediaPreference(service: AudioForegroundService) = MediaPreferences(service)
fun provideNotification(service: AudioForegroundService, sessionCompat: MediaSessionCompat) = AudioForegroundNotification21(service, sessionCompat)

fun provideMediaAudioFocus(service: AudioForegroundService): MediaAudioFocusCompat {
    return if (isOreoOrLater())
        MediaAudioFocus(service)
    else
        MediaAudioFocusPre(service)

}

fun provideNotificationManager(service: AudioForegroundService): AudioForegroundNotificationManager {
    return AudioForegroundNotificationManager(service, provideNotification(service, provideMediaSessionCompat(service)))
}

fun provideAudioPlayer(service: AudioForegroundService): AudioPlayer {
    return AudioPlayer(service, provideMediaSessionConnectorAdapter(service, provideMediaSessionCompat(service)), provideSimpleExoPlayer(service), provideMediaPreference(service))

}

fun provideFocusRequest(service: AudioForegroundService, mediaAudioFocus: MediaAudioFocusCompat, audioPlayer: AudioPlayer): FocusRequestImp {
    return FocusRequestImp(mediaAudioFocus, service, audioPlayer)
}