package com.example.mediaplayer.data

import android.content.Context
import com.example.mediaplayer.extensions.getItem
import com.example.mediaplayer.extensions.getSharedPrefrence
import com.example.mediaplayer.extensions.putItem
import javax.inject.Inject


private const val TAG = "MediaPreferences"
private const val CURRENT_WINDOW = "${TAG}.current window"
private const val PLAYBACK_POSITION = "${TAG}.playback position"

class MediaPreferences @Inject constructor(context: Context) {
    private val preferences = context.getSharedPrefrence(TAG)
    fun getCurrentTrack(): Int {
        return preferences!!.getItem(CURRENT_WINDOW, 0)
    }

    fun getCurrentPosition(): Long {
        return preferences!!.getItem(PLAYBACK_POSITION, 0)
    }

    fun setCurrentTrack(id: Int) {
        return preferences!!.putItem(CURRENT_WINDOW, id)
    }

    fun setCurrentPosition(position: Long) {
        return preferences!!.putItem(PLAYBACK_POSITION, position)
    }
}