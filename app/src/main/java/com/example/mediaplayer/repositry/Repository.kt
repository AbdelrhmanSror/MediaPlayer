package com.example.mediaplayer.repositry

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.mediaplayer.model.PlayListModel
import java.util.*


class Repository(private val application: Application) {
    /**
     * method to get list of audio uris on the device
     *
     * @return list of audio uris
     */
    // query failed, handle error.
    // no media on the device
    //if there is no album for this audio replace it with default one
    val mediaData: List<PlayListModel>?
        get() {
            val contentResolver = application.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            } else {
                val playLists = ArrayList<PlayListModel>()
                with(cursor)
                {
                    do {
                        val defaultImageUri = "android.resource://" + application.packageName + "/drawable/default_image.jpg"
                        val audioUri = getAudioUri()
                        playLists.add(PlayListModel(getAudioName(), getAudioArtist(), audioUri, getAlbumImageUri(defaultImageUri), getDuration(audioUri)))
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

    private fun Cursor.getAlbumImageUri(defaultImageUru: String): String {
        val albumPath = getAlbumArtPath(getString(getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))
        //if there is no photo for the song then use preChosen one
        if (albumPath.isNullOrEmpty()) {
            return defaultImageUru
        }
        return albumPath
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
            contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=?",
                    arrayOf(albumId), null)
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
