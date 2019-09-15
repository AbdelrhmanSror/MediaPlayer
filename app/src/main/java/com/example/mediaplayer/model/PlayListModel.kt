package com.example.mediaplayer.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * class represent an item in playlist
 */
@Parcelize
data class PlayListModel(val Title: String, val actor: String, val audioUri: Uri, val albumCoverUri: String?, val duration: Long) : Parcelable
