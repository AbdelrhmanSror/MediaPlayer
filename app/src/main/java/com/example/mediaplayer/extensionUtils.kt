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

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat


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

fun <T> PagedList<out T>?.toArrayList(): List<T> {

    return if (this.isNullOrEmpty()) {
        emptyList()
    } else {
        ArrayList(this)
    }
}

//start animation of like or dislike audio
fun ImageButton.startFavouriteAnimation(addToFavourite: Boolean) {
    this.apply {
        val animatedVector = if (addToFavourite) {
            AnimatedVectorDrawableCompat.create(context, R.drawable.ic_favourite)

        } else {
            AnimatedVectorDrawableCompat.create(context, R.drawable.ic_favourite_stroke)
        }
        setImageDrawable(animatedVector)
        animatedVector?.start()
    }

}

/**
 * call it from background
 */
fun <T> MutableLiveData<T>.notifyObserver() {
    this.postValue(this.value)
}

fun Activity.disableActionBarTitle() {
    //to disable the action bar title and use my own custom title.
    (this as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
}


/**
 * extension function to check if user granted the permissions
 */
fun Application.isAudioFilesPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}
