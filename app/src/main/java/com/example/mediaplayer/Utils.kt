package com.example.mediaplayer

import android.os.Build


fun isMarshmallow(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

fun isNougat(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}


fun isOreo(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

fun isP(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}

fun isQ(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}
