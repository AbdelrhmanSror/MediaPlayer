package com.example.mediaplayer

import android.os.Build

fun isNougat(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}

fun isOreo(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}


fun <T> HashSet<T>.updateList(updatedPlayerState: HashSet<T>) {
    if (updatedPlayerState.size > this.size) {
        updatedPlayerState.filterIndexed { _, ipLayerState ->
            if (!(this.contains(ipLayerState))) {
                this.add(ipLayerState)

            }
            true
        }
    } else if (updatedPlayerState.size < this.size) {
        this.filterIndexed { _, ipLayerState ->
            if (!(this.contains(ipLayerState))) {
                this.remove(ipLayerState)

            }
            true
        }
    }

}