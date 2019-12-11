package com.example.mediaplayer

import android.os.Build

fun isNougat(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}

fun isOreo(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}


fun <K, V> HashMap<K, V>.updateList(updatedPlayerState: HashMap<K, V>) {
    val deletedItems = arrayListOf<K>()
    when {
        updatedPlayerState.size > this.size -> {
            updatedPlayerState.filter {
                if (!(this.contains(it.key))) {
                    this[it.key] = it.value

                }
                true
            }
        }
        updatedPlayerState.size < this.size -> {
            this.filter {
                if (!(updatedPlayerState.contains(it.key))) {
                    deletedItems.add(it.key)
                }
                true
            }
            for (i in 0 until deletedItems.size) {
                this.remove(deletedItems[i])

            }

        }
    }

}