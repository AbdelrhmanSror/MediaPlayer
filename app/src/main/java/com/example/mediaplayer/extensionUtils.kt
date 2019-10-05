/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mediaplayer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


/**
 * extension function for starting foreground service
 */
fun Context.startForeground(foregroundIntent: Intent) {
    //Start service:
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(foregroundIntent)

    } else {
        startService(foregroundIntent)

    }
}

/**
 * add 0 before the digit if it was single number
 */
fun Int.twoDigitNumber(): String {
    Log.v("theNumber", "$this")
    return if (this < 10) {
        ("0${this}")
    } else
        this.toString()
}

