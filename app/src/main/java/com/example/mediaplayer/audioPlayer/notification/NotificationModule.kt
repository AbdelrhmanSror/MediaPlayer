package com.example.mediaplayer.audioPlayer.notification

import com.example.mediaplayer.di.PerService
import com.example.mediaplayer.shared.isNougat
import com.example.mediaplayer.shared.isOreo
import dagger.Lazy
import dagger.Module
import dagger.Provides

@Module
internal object NotificationModule {

    @Provides
    @PerService
    @JvmStatic
    internal fun provideNotificationImpl(notificationImpl26: Lazy<AudioForegroundNotification26>, notificationImpl24: Lazy<AudioForegroundNotification24>, notificationImpl: Lazy<AudioForegroundNotification>

    ): INotification {
        return when {
            isOreo() -> notificationImpl26.get()
            isNougat() -> notificationImpl24.get()
            else -> notificationImpl.get()
        }
    }

}