package com.example.mediaplayer.repositry

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.mediaplayer.DeviceAudioFile
import com.example.mediaplayer.database.FavouriteSongEntity
import com.example.mediaplayer.database.PlayerDatabase
import com.example.mediaplayer.database.SongEntity
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toFavouriteSongEntity
import com.example.mediaplayer.model.toSongEntity


class Repository(private val application: Context) {

    private val database = PlayerDatabase.getInstance(application)


    fun getFavouriteSongs(): LiveData<List<FavouriteSongEntity>> {
        return database.favouriteSongsDao().getAllFavouriteSong()
    }

    fun insertIntoFavouriteSongs(songModel: SongModel) {
        database.runInTransaction {
            database.songDao().updateFavourite(songModel.title, true)
            database.favouriteSongsDao().insertFavouriteSong(songModel.toFavouriteSongEntity())

        }
    }

    fun deleteFromFavouriteSongs(title: String) {
        database.runInTransaction {
            database.favouriteSongsDao().deleteFavouriteSong(title)
            database.songDao().updateFavourite(title, false)

        }
    }

    private fun insertListOfSongs(songs: List<SongEntity>) {
        database.songDao().insertAll(songs)
    }

    fun getListOfSongs(): LiveData<List<SongEntity>> {
        return database.songDao().getAllSongs()
    }

    fun getSong(title: String): SongEntity {
        return database.songDao().getSong(title)
    }

    //inserting the audio file on user device into database so our local database will become our source of true
    fun insertAudioIntoDatabase() {
        val deviceAudioFile = DeviceAudioFile(application)
        var limit = 15
        var offset = 0
        while (true) {
            val listOfSong = deviceAudioFile.getAudios(limit, offset).toSongEntity()
            if (listOfSong.isNullOrEmpty()) {
                break
            }
            insertListOfSongs(listOfSong)
            offset += limit
            limit *= 3
        }
    }
}
