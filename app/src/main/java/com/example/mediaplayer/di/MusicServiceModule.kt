package com.example.mediaplayer.di

import android.app.Service
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.Lifecycle
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.audioPlayer.Noisy
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.google.android.exoplayer2.ExoPlayerFactory
import dagger.Binds
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
        @JvmStatic
        @ServiceLifecycle
        internal fun provideLifecycle(instance: AudioForegroundService): Lifecycle = instance.lifecycle

        @Provides
        @JvmStatic
        @PerService
        internal fun provideMediaSession(instance: AudioForegroundService): MediaSessionCompat {
            return MediaSessionCompat(instance, instance.packageName)
        }

        @Provides
        @JvmStatic
        @PerService
        internal fun providePlayer(service: Service, mediaSessionCompat: MediaSessionCompat, noisy: Noisy): AudioPlayer {
            return AudioPlayer(service, mediaSessionCompat, ExoPlayerFactory.newSimpleInstance(service), noisy)
        }


    }

}