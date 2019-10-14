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

package com.example.mediaplayer

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.mediaplayer.model.SongModel

class DeviceAudioFile(private val application: Context) {
    /**
     * method to get list of audio uris on the device
     *
     * @return list of audio uris
     */
    // query failed, handle error.
    // no media on the device
    fun getAudios(limit: Int, offset: Int): List<SongModel> {
        val contentResolver = application.contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uri, arrayOf(
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID
        ), MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, "${MediaStore.Audio.Media._ID}  limit $limit offset $offset")
        val playLists: MutableList<SongModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return playLists
        } else {

            with(cursor)
            {
                do {
                    val audioUri = getAudioUri()
                    playLists.add(SongModel(getAudioName(), getAudioArtist(), audioUri, getAlbumImageUri(), getDuration(audioUri)))
                } while (moveToNext())
                close()
                return playLists
            }


        }
    }


    private fun Cursor.getAudioUri(): Uri {
        val audioId = this.getLong(getColumnIndex(MediaStore.Audio.Media._ID))
        return ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId)
    }

    private fun Cursor.getAudioName(): String {
        return getString(getColumnIndex(MediaStore.Audio.Media.TITLE))
    }

    private fun Cursor.getAudioArtist(): String {
        return getString(getColumnIndex(MediaStore.Audio.Media.ARTIST))
    }

    private fun Cursor.getAlbumImageUri(): String? {
        return getAlbumArtPath(getString(getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))
    }

    private fun getDuration(audioUri: Uri): Long {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(application, audioUri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr.toLong()
    }

    /**
     * method to get the album art image for specific audio
     *
     * @param albumId of the audio to get its specific album art
     * @return uri of the album art image
     */
    private fun getAlbumArtPath(albumId: String): String? {
        val cursor = with(application) {
            contentResolver.query(android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    kotlin.arrayOf(android.provider.MediaStore.Audio.Albums._ID, android.provider.MediaStore.Audio.Albums.ALBUM_ART),
                    android.provider.MediaStore.Audio.Albums._ID + "=?",
                    kotlin.arrayOf(albumId), null)
        }
        cursor?.run {
            if (moveToFirst()) {
                val path = getString(getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                close()
                return path
            }

        }
        return null
    }


}