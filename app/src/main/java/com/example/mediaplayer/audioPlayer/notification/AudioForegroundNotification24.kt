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

package com.example.mediaplayer.audioPlayer.notification

import android.app.Service
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.example.mediaplayer.R
import com.example.mediaplayer.extensions.twoDigitNumber

@RequiresApi(Build.VERSION_CODES.N)
internal open class AudioForegroundNotification24 constructor(
        service: Service,
        mediaSession: MediaSessionCompat

) : AudioForegroundNotification21(service, mediaSession) {

    override fun startChronometer(bookmark: Long) {
        builder.setWhen(System.currentTimeMillis() - bookmark)
                .setShowWhen(true)
                .setUsesChronometer(true)
        builder.setSubText(null)
    }

    override fun stopChronometer(bookmark: Long) {
        builder.setWhen(0)
                .setShowWhen(false)
                .setUsesChronometer(false)
        val min = (bookmark / 60).toInt().twoDigitNumber()
        val sec = (bookmark % 60).toInt().twoDigitNumber()
        val duration = service.getString(R.string.duration_format, min, sec)
        builder.setSubText(duration)
    }


}