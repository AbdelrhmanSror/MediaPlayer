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
    fun getAudios(): List<SongModel> {
        val contentResolver = application.contentResolver
        val sortOrder: String = MediaStore.Audio.Media.TITLE + " ASC"
        val artWorkUri = Uri.parse("content://media/external/audio/albumart")

        val cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, sortOrder)

        val playLists: MutableList<SongModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return playLists
        } else {

            with(cursor)
            {
                val audioImageColumn = getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val audioId = getColumnIndex(MediaStore.Audio.Media._ID)
                val audioNameColumn = getColumnIndex(MediaStore.Audio.Media.TITLE)
                val audioArtistColumn = getColumnIndex(MediaStore.Audio.Media.ARTIST)
                do {
                    val id = getLong(audioId)
                    val name = getString(audioNameColumn)
                    val artist = getString(audioArtistColumn)
                    val albumId = getString(audioImageColumn)
                    val audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val imageUri = ContentUris.withAppendedId(artWorkUri, albumId.toLong()).toString()
                    playLists.add(SongModel(name, artist, audioUri, imageUri))
                } while (moveToNext())
                close()
            }
            return playLists


        }
    }
    /*val mmr = MediaMetadataRetriever()
    private fun getDuration(audioUri: Uri): Long {
        mmr.setDataSource(application, audioUri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr.toLong()
    }
*/
    /**
     * method to get the album art image for specific audio
     *
     * @param albumId of the audio to get its specific album art
     * @return uri of the album art image
     */
    /* private fun getAlbumArtPath(albumId: List<String>): ArrayList<String> {
         val cursor = with(application) {
             contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                     arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                     MediaStore.Audio.Albums._ID + "=?",null, null)
         }
         val listOfImage = ArrayList<String>()

         cursor?.run {
             val albumArtColumn = getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)

             if (moveToFirst() && count >= 1) {
                 do {
                     val image = getString(albumArtColumn)
                     listOfImage.add(image)

                 } while (cursor.moveToNext())
             }
             close()
         }
         return listOfImage
     }*/
}