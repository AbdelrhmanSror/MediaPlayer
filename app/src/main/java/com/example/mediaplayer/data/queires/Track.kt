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

@file:Suppress("DEPRECATION")

package com.example.mediaplayer.data.queires

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.*
import android.util.Log
import androidx.core.database.getStringOrNull
import com.example.mediaplayer.data.queires.Columns.ALBUM_ARTIST
import com.example.mediaplayer.model.SongModel

class Track(private val application: Context) {
    /**
     * method to get list of audio uris on the device
     *
     * @return list of audio uris
     */
    // query failed, handle error.
    // no media on the device
    @SuppressLint("InlinedApi")
    fun getAll(): List<SongModel> {
        val contentResolver = application.contentResolver
        val projection = arrayOf(_ID, ARTIST_ID, ALBUM_ID,
                TITLE,
                ARTIST,
                ALBUM,
                ALBUM_ARTIST,
                DURATION,
                DATA, YEAR,
                TRACK,
                DATE_ADDED,
                DATE_MODIFIED)
        val sortOrder = "$TITLE ASC"
        val artWorkUri = Uri.parse("content://media/external/audio/albumart")

        val cursor = contentResolver.query(EXTERNAL_CONTENT_URI,
                projection, "$IS_MUSIC!= 0", null, sortOrder)

        val playLists: MutableList<SongModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return playLists
        } else {

            with(cursor)
            {
                val albumIdColumns = getColumnIndex(ALBUM_ID)
                val audioIdColumn = getColumnIndex(_ID)
                val titleColumn = getColumnIndex(TITLE)
                val artistColumn = getColumnIndex(ARTIST)
                val artistIdColumn = getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_ID)
                val albumColumn = getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)
                val albumArtistColumn = getColumnIndex(ALBUM_ARTIST)
                val durationColumn = getColumnIndex(DURATION)
                val dateAddedColumn = getColumnIndex(DATE_ADDED)
                val dateModifiedColumn = getColumnIndex(DATE_MODIFIED)
                do {
                    val id = getLong(audioIdColumn)
                    val title = getString(titleColumn)
                    val artist = getString(artistColumn)
                    val albumId = getLong(albumIdColumns)
                    val artistId = getLong(artistIdColumn)
                    val album = getString(albumColumn)
                    val albumArtist = getStringOrNull(albumArtistColumn) ?: artist
                    val duration = getLong(durationColumn)
                    val dateAdded = getLong(dateAddedColumn)
                    val dateModified = getLong(dateModifiedColumn)
                    val audioUri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id)
                    val imageUri = ContentUris.withAppendedId(artWorkUri, albumId).toString()
                    Log.v("tracks", " ${id} ${title} ${albumId}  ${artist}  ${artistId} ")

                    playLists.add(SongModel(id, title, artist, audioUri, imageUri, duration, albumId, artistId, album, albumArtist, dateAdded, dateModified))
                } while (moveToNext())
                close()
            }
            return playLists


        }
    }

}