package com.example.mediaplayer.repositry

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.mediaplayer.data.queires.Track
import com.example.mediaplayer.database.PlayerDatabase
import com.example.mediaplayer.database.SongEntity
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toFavouriteSongEntity
import com.example.mediaplayer.model.toSongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class TracksRepository @Inject constructor(private val application: Application, private val database: PlayerDatabase) : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    fun getFavouriteSongs(): LiveData<List<SongEntity>> {
        return database.favouriteSongsDao().getAllFavouriteSong()
    }

    fun addFavouriteSong(songModel: SongModel) {
        launch {
            database.favouriteSongsDao().insertFavouriteSong(songModel.toFavouriteSongEntity())

        }
    }

    fun removeFromFavouriteSongs(id: Long) {
        launch {
            database.favouriteSongsDao().deleteFavouriteSong(id)

        }
    }

    private fun insertSongs(newListOfSongs: List<SongEntity>) {
        launch {
            var oldlistOfSong = database.songDao().getAllSongs()
            val dao = database.songDao()
            //if there was any song deleted we update database based on that
            oldlistOfSong = withContext(Dispatchers.IO) {
                oldlistOfSong.forEach {
                    if (!newListOfSongs.contains(it)) {
                        dao.deleteSong(it.id)
                    }
                }
                dao.getAllSongs()
            }
            //if there was any new song added we added to database
            launch {
                newListOfSongs.forEach {
                    if (!oldlistOfSong.contains(it)) {
                        dao.insert(it)

                    }
                }
            }

        }
    }

    fun observeSongs(): LiveData<List<SongEntity>> {
        return database.songDao().getAllSongsLiveData()
    }

    suspend fun getSongs(): List<SongEntity> {
        return database.songDao().getAllSongs()
    }


    //inserting the audio file on user device into database so our local database will become our source of true
    fun insertAudioIntoDatabase() {
        val track = Track(application)
        /* val genres=Genre(application)
         genres.getAll().forEach {
             Log.v("genres", " ${it.id} ${it.name} ${it.size}")

         }*/
        val listOfSong = track.getAll().toSongEntity()
        insertSongs(listOfSong)

    }
}

