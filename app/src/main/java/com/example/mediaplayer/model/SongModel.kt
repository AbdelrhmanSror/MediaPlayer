package com.example.mediaplayer.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore.Images.Media.getBitmap
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.example.mediaplayer.database.FavouriteSongEntity
import com.example.mediaplayer.database.SongEntity
import com.example.mediaplayer.toUri
import kotlinx.android.parcel.Parcelize


/**
 * class represent an item in playlist
 */
@Parcelize
data class SongModel(val title: String, val actor: String, val audioUri: Uri, val albumCoverUri: String?, val duration: Long, var isFavourite: Boolean = false) : Parcelable

fun SongModel.getMediaDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
            .setMediaUri(this.audioUri)
            .setIconUri(this.albumCoverUri.toUri())
            .setTitle(this.title)
            .setSubtitle(this.actor)
            .build()
}

fun SongModel.toFavouriteSongEntity(): FavouriteSongEntity {
    return FavouriteSongEntity(name = this.title)

}

fun List<SongModel>.toSongEntity(): List<SongEntity> {
    return map { SongEntity(name = it.title, actor = it.actor, audioUri = (it.audioUri).toString(), albumCoverUri = it.albumCoverUri, duration = it.duration, isFavourite = it.isFavourite) }

}