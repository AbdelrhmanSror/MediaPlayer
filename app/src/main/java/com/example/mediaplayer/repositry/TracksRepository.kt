/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.repositry

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.mediaplayer.data.queires.Track
import com.example.mediaplayer.database.PlayerDatabase
import com.example.mediaplayer.model.SongEntity
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toFavouriteSongEntity
import com.example.mediaplayer.model.toSongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TracksRepository constructor(private val application: Application, private val database: PlayerDatabase) : CoroutineScope by CoroutineScope(Dispatchers.IO) {

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
            val time0 = System.currentTimeMillis()
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
            newListOfSongs.forEach {
                if (!oldlistOfSong.contains(it)) {
                    dao.insert(it)

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

