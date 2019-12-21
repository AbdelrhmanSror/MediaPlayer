package com.example.mediaplayer.data.queires

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Genres.*
import androidx.core.database.getStringOrNull
import com.example.mediaplayer.model.ArtistModel
import com.example.mediaplayer.model.GenreModel
import com.example.mediaplayer.model.SongModel


class Genre(private val application: Context) {
    private val contentResolver = application.contentResolver


    // query failed, handle error.
    // no media on the device
    fun getAll(): List<GenreModel> {
        val projection = arrayOf(_ID, NAME)
        val cursor = contentResolver.query(EXTERNAL_CONTENT_URI,
                projection, null, null, null)

        val genres: MutableList<GenreModel> = mutableListOf()
        if (cursor == null || !cursor.moveToFirst()) {
            return genres
        } else {

            with(cursor)
            {
                val genreIdColumn = getColumnIndex(BaseColumns._ID)
                val genreNameColumn = getColumnIndex(MediaStore.Audio.GenresColumns.NAME)
                do {
                    val name = getStringOrNull(genreNameColumn)?.capitalize() ?: ""
                    val id = getLong(genreIdColumn)
                    genres.add(GenreModel(id, name, 0))
                } while (moveToNext())
                close()
            }
            return genres.map {
                it.withSongs(genreCount(it.id))
            }


        }
    }

    private fun genreCount(genreId: Long): Int {
        val projection = arrayOf(Members._ID, Members.AUDIO_ID)
        val cursor = contentResolver.query(Members.getContentUri("external", genreId),
                projection, "${MediaStore.Audio.Media.IS_MUSIC}!= 0", null, null)
        if (cursor == null || !cursor.moveToFirst()) {
            return -1
        } else {
            with(cursor)
            {
                val count = count
                close()
                return count
            }

        }

    }

    fun getRelatedArtist(id: Long): List<ArtistModel> {
        val projection = arrayOf(
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST,
                Columns.ALBUM_ARTIST)
        val cursor = contentResolver.query(Members.getContentUri("external", id),
                projection, "${MediaStore.Audio.Media.IS_MUSIC}!= 0", null, null)

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

    @SuppressLint("InlinedApi")
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

        val cursor = contentResolver.query(Members.getContentUri("external", id),
                projection, "${MediaStore.Audio.Media.IS_MUSIC}!= 0 ", null, null)

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
                    val audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
                    val imageUri = ContentUris.withAppendedId(artWorkUri, albumId).toString()
                    playLists.add(SongModel(songId, title, artist, audioUri, imageUri, duration, albumId, artistId, album, albumArtist, dateAdded, dateModified))
                } while (moveToNext())
                close()
            }
            return playLists


        }
    }

}