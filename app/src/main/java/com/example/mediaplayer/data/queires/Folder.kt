/*
package com.example.mediaplayer.data.queires

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import com.example.mediaplayer.model.ArtistModel
import com.example.mediaplayer.model.GenreModel
import com.example.mediaplayer.model.SongModel
import android.provider.MediaStore.Audio.Media.*


class Folder(private val application: Context) {
    private val contentResolver = application.contentResolver


    // query failed, handle error.
    // no media on the device
    fun getAll(): List<GenreModel> {
        val projection = arrayOf(DATA)
        val cursor = contentResolver.query(EXTERNAL_CONTENT_URI,
                projection, "$IS_MUSIC!= 0", null, null)

        val genres: MutableList<GenreModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return genres
        } else {

            with(cursor)
            {
                val genreIdColumn = getColumnIndex(BaseColumns._ID)
                val genreNameColumn = getColumnIndex(MediaStore.Audio.GenresColumns.NAME)

                do {

                    val id = getLong(genreIdColumn)
                    val name = getString(genreNameColumn)

                    genres.add(GenreModel(id, name, 0))
                } while (moveToNext())
                close()
            }
            return genres


        }
    }

    fun getRelatedArtist(id: Long): List<ArtistModel> {
        val projection = arrayOf(
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                Columns.ALBUM_ARTIST)
        val cursor = contentResolver.query(MediaStore.Audio.Genres.Members.getContentUri("external", id),
                projection, "$IS_MUSIC!= 0", null, null)

        val artists: MutableList<ArtistModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return artists
        } else {

            with(cursor)
            {
                val artistColumn = getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val artistIdColumn = getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_ID)
                val albumArtistColumn = getColumnIndex(Columns.ALBUM_ARTIST)
                do {

                    val artist = getString(artistColumn)
                    val artistId = getLong(artistIdColumn)
                    val albumArtist = getStringOrNull(albumArtistColumn) ?: artist
                    artists.add(ArtistModel(artistId, artist, albumArtist, 0))
                } while (moveToNext())
                close()
            }
            return artists.groupBy { it.id }.map {
                val artist = it.value[0]
                artist.withSongs(it.value.size)
            }


        }
    }

    fun getListSongs(id: Long): List<SongModel> {
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                Columns.ALBUM_ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED)
        val artWorkUri = Uri.parse("content://media/external/audio/albumart")

        val cursor = contentResolver.query(MediaStore.Audio.Genres.Members.getContentUri("external", id),
                projection, "$IS_MUSIC!= 0 ", null, null)

        val playLists: MutableList<SongModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return playLists
        } else {

            with(cursor)
            {
                val albumIdColumns = getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val audioIdColumn = getColumnIndex(MediaStore.Audio.Media._ID)
                val titleColumn = getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val artistIdColumn = getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_ID)
                val albumColumn = getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)
                val albumArtistColumn = getColumnIndex(Columns.ALBUM_ARTIST)
                val durationColumn = getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dateAddedColumn = getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedColumn = getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                do {
                    val songId = getLong(audioIdColumn)
                    val title = getString(titleColumn)
                    val artist = getString(artistColumn)
                    val albumId = getLong(albumIdColumns)
                    val artistId = getLong(artistIdColumn)
                    val album = getString(albumColumn)
                    val albumArtist = getStringOrNull(albumArtistColumn) ?: artist
                    val duration = getLong(durationColumn)
                    val dateAdded = getLong(dateAddedColumn)
                    val dateModified = getLong(dateModifiedColumn)
                    val audioUri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, songId)
                    val imageUri = ContentUris.withAppendedId(artWorkUri, albumId).toString()
                    playLists.add(SongModel(songId, title, artist, audioUri, imageUri, duration, albumId, artistId, album, albumArtist, dateAdded, dateModified))
                } while (moveToNext())
                close()
            }
            return playLists


        }
    }

*/
