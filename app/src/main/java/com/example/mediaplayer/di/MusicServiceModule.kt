package com.example.mediaplayer.di

import android.app.Service
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.Lifecycle
import com.example.mediaplayer.audioForegroundService.AudioForegroundService
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.MediaSessionConnectorAdapter
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocus
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusCompat
import com.example.mediaplayer.audioPlayer.audioFocus.MediaAudioFocusPre
import com.example.mediaplayer.data.MediaPreferences
import com.example.mediaplayer.shared.isOreo
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Scope


@Qualifier
annotation class ServiceLifecycle

@Scope
annotation class PerService

@Qualifier
annotation class ServiceContext

@Module
abstract class MusicServiceModule {

    @Binds
    @ServiceContext
    internal abstract fun provideContext(instance: AudioForegroundService): Context

    @Binds
    internal abstract fun provideService(instance: AudioForegroundService): Service


    @Module
    companion object {
        @Provides
        @PerService
        @JvmStatic
        @ServiceLifecycle
        internal fun provideLifecycle(instance: AudioForegroundService): Lifecycle = instance.lifecycle

        @Provides
        @PerService
        @JvmStatic
        internal fun provideMediaSession(instance: AudioForegroundService): MediaSessionCompat {
            return MediaSessionCompat(instance, instance.packageName)
        }

        @Provides
        @PerService
        @JvmStatic
        internal fun provideMediaSessionConnector(mediaSessionCompat: MediaSessionCompat): MediaSessionConnector {
            return MediaSessionConnector(mediaSessionCompat)
        }


        @Provides
        @JvmStatic
        @PerService
        internal fun provideMediaFocus(mediaAudioFocus: Lazy<MediaAudioFocus>, mediaAudioFocusPre: Lazy<MediaAudioFocusPre>
        ): MediaAudioFocusCompat {
            return when {
                isOreo() -> mediaAudioFocus.get()
                else -> mediaAudioFocusPre.get()
            }
        }

        @Provides
        @JvmStatic
        @PerService
        internal fun provideMediaPrefrences(context: Context): MediaPreferences {
            return MediaPreferences(context)
        }

        @Provides
        @JvmStatic
        @PerService
        internal fun provideExoPlayer(service: AudioForegroundService): SimpleExoPlayer {
            return ExoPlayerFactory.newSimpleInstance(service)
        }

        @Provides
        @JvmStatic
        @PerService
        internal fun provideAudioPlayer(service: AudioForegroundService, player: SimpleExoPlayer, mediaSessionConnectorAdapter: MediaSessionConnectorAdapter, mediaPreferences: MediaPreferences): AudioPlayer {
            return AudioPlayer(service, mediaSessionConnectorAdapter, player, mediaPreferences)
        }

        @Provides
        @JvmStatic
        @PerService
        internal fun provideMediaSessionConnectorAdapter(mediaSessionCompat: MediaSessionCompat, mediaSessionConnector: MediaSessionConnector): MediaSessionConnectorAdapter {
            return MediaSessionConnectorAdapter(mediaSessionCompat, mediaSessionConnector)
        }


    }

}