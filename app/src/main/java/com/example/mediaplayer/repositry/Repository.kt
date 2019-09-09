package com.example.mediaplayer.repositry

import android.app.Application
import android.content.ContentUris
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
                        val thisId = getLong(getColumnIndex(MediaStore.Audio.Media._ID))
                        val title = getString(getColumnIndex(MediaStore.Audio.Media.TITLE))
                        val actor = getString(getColumnIndex(MediaStore.Audio.Media.ARTIST))
                        var albumPath = getAlbumArtPath(getString(getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))
                        if (albumPath.isNullOrEmpty()) {
                            albumPath = "android.resource://" + application.packageName + "/drawable/default_image.jpg"
                        }
                        val contentUri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId)
                        playLists.add(PlayListModel(title, actor, contentUri, albumPath))
                    } while (moveToNext())
                    close()
                    return playLists
                }


            }
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
