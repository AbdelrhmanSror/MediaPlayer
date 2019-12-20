package com.example.mediaplayer.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity


fun Activity.disableActionBarTitle() {
    //to disable the action bar title and use my own custom title.
    (this as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
}


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

fun Context.getSharedPrefrence(prefName: String): SharedPreferences? {
    return getSharedPreferences(prefName, Context.MODE_PRIVATE)
}


