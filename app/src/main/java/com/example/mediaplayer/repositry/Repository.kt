package com.example.mediaplayer.repositry

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mediaplayer.DeviceAudioFile
import com.example.mediaplayer.database.PlayerDatabase
import com.example.mediaplayer.database.SongEntity
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toFavouriteSongEntity
import com.example.mediaplayer.model.toSongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class Repository @Inject constructor(private val application: Application, private val database: PlayerDatabase) : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    fun getFavouriteSongs(): LiveData<List<SongEntity>> {
        return database.favouriteSongsDao().getAllFavouriteSong()
    }

    fun addFavouriteSong(songModel: SongModel) {
        launch {
            database.favouriteSongsDao().insertFavouriteSong(songModel.toFavouriteSongEntity())

        }
    }

    fun removeFromFavouriteSongs(title: String) {
        launch {
            database.favouriteSongsDao().deleteFavouriteSong(title)

        }
    }

    fun insertSongs(songs: List<SongEntity>) {
        launch {
            Log.v("updatingPlaylist", "updating......")
            database.songDao().insertAll(songs)
            Log.v("updatingPlaylist", "done......")

        }
    }

    fun observeSongs(): LiveData<List<SongEntity>> {
        return database.songDao().getAllSongsLiveData()
    }

    fun getSongs(): List<SongEntity> {
        return database.songDao().getAllSongs()
    }


    //inserting the audio file on user device into database so our local database will become our source of true
    suspend fun insertAudioIntoDatabase() {
        val deviceAudioFile = DeviceAudioFile(application)
        val listOfSong = deviceAudioFile.getAudios().toSongEntity()
        insertSongs(listOfSong)

    }
}

