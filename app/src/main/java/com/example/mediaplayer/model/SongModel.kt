package com.example.mediaplayer.model

import android.net.Uri
import android.os.Parcelable
import com.example.mediaplayer.database.FavouriteSongEntity
import com.example.mediaplayer.database.SongEntity
import kotlinx.android.parcel.Parcelize


/**
 * class represent an item in playlist
 */
@Parcelize
data class SongModel(val title: String, val actor: String, val audioUri: Uri, val albumCoverUri: String?, val duration: Long, var isFavourite: Boolean = false) : Parcelable

fun SongModel.toFavouriteSongEntity(): FavouriteSongEntity {
    return FavouriteSongEntity(name = this.title)

}

fun List<SongModel>.toSongEntity(): List<SongEntity> {
    return map { SongEntity(name = it.title, actor = it.actor, audioUri = (it.audioUri).toString(), albumCoverUri = it.albumCoverUri, duration = it.duration, isFavourite = it.isFavourite) }

}