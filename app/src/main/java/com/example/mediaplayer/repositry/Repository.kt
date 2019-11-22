package com.example.mediaplayer.repositry

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.mediaplayer.DeviceAudioFile
import com.example.mediaplayer.database.PlayerDatabase
import com.example.mediaplayer.database.SongEntity
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toFavouriteSongEntity
import com.example.mediaplayer.model.toSongEntity

class Repository private constructor(private val application: Context) {

    private var database: PlayerDatabase = Room.databaseBuilder(application.applicationContext, PlayerDatabase::class.java, "SongList").fallbackToDestructiveMigration().build()

    companion object {
        @Volatile
        private var INSTANCE: Repository? = null

        fun getRepository(app: Application): Repository {
            return INSTANCE ?: synchronized(this) {
                Repository(app).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun getFavouriteSongs(): LiveData<List<SongEntity>> {
        return database.favouriteSongsDao().getAllFavouriteSong()
    }

    fun addFavouriteSong(songModel: SongModel) {
        database.runInTransaction {
            database.songDao().updateFavourite(songModel.title, true)
            database.favouriteSongsDao().insertFavouriteSong(songModel.toFavouriteSongEntity())

        }
    }

    fun removeFromFavouriteSongs(title: String) {
        database.runInTransaction {
            database.favouriteSongsDao().deleteFavouriteSong(title)
            database.songDao().updateFavourite(title, false)

        }
    }

    private fun insertSongs(songs: List<SongEntity>) {
        database.songDao().insertAll(songs)
    }

    fun observeSongs(): LiveData<List<SongEntity>> {
        return database.songDao().getAllSongsLiveData()
    }

    fun getSongs(): List<SongEntity> {
        return database.songDao().getAllSongs()
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
            insertSongs(listOfSong)
            offset += limit
            limit *= 3
        }
    }
}
