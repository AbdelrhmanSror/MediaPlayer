/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.model

import android.net.Uri
import android.os.Parcelable
import android.support.v4.media.MediaDescriptionCompat
import com.example.mediaplayer.database.FavouriteSongEntity
import com.example.mediaplayer.extensions.toUri
import kotlinx.android.parcel.Parcelize

/**
 * class represent an item in playlist
 */
@Parcelize
data class SongModel(val id: Long,
                     val title: String,
                     val artist: String,
                     val audioUri: Uri,
                     val albumCoverUri: String?,
                     val duration: Long? = null, val albumId: Long? = null,
                     val artistId: Long? = null,
                     val album: String? = null,
                     val albumArtist: String? = null,
                     val dateAdded: Long? = null,
                     val dateModified: Long? = null,
                     var isFavourite: Boolean = false) : Parcelable

fun SongModel.getMediaDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
            .setMediaUri(this.audioUri)
            .setIconUri(this.albumCoverUri.toUri())
            .setTitle(this.title)
            .setSubtitle(this.artist)
            .build()
}

fun SongModel.toFavouriteSongEntity(): FavouriteSongEntity {
    return FavouriteSongEntity(songId = this.id, name = this.title)

}

fun List<SongModel>.toSongEntity(): List<SongEntity> {
    return map { SongEntity(id = it.id, name = it.title, actor = it.artist, audioUri = (it.audioUri).toString(), albumCoverUri = it.albumCoverUri, duration = it.duration) }

}