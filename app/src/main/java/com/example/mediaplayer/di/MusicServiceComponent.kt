package com.example.mediaplayer.di

import android.content.Context
import com.example.mediaplayer.audioPlayer.notification.NotificationModule
import com.example.mediaplayer.foregroundService.AudioForegroundService
import dagger.BindsInstance
import dagger.Component

internal fun AudioForegroundService.inject() {
    DaggerAudioForegroundServiceComponent.factory().create(this)
            .inject(this)
}

@Component(modules = [
    MusicServiceModule::class,
    NotificationModule::class
])
@PerService
interface AudioForegroundServiceComponent {

    fun inject(instance: AudioForegroundService)

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance instance: AudioForegroundService, @BindsInstance context: Context = instance): AudioForegroundServiceComponent

    }
}