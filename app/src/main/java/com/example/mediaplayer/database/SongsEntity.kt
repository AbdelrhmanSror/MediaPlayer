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

package com.example.mediaplayer.database

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mediaplayer.model.SongModel


@Entity(tableName = "songs", indices = [Index(value = ["name"], unique = true)])
data class SongEntity(
        @PrimaryKey var name: String,
        var actor: String,
        var audioUri: String,
        var albumCoverUri: String?,
        var duration: Long?
        , var isFavourite: Boolean = false)

@Dao
interface SongsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(Songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE name=:name")
    fun deleteSong(name: String)

    @Query("SELECT * FROM songs")
    fun getAllSongsLiveData(): LiveData<List<SongEntity>>

    @Query("SELECT * FROM songs")
    fun getAllSongs(): List<SongEntity>

    @Query("UPDATE songs SET isFavourite=:isFavourite WHERE name=:name")
    fun updateFavourite(name: String, isFavourite: Boolean)

    @Query("DELETE FROM songs")
    fun clear()
}

fun List<SongEntity>.toSongModel(): List<SongModel> {
    return map {
        SongModel(title = it.name, actor = it.actor, audioUri = Uri.parse(it.audioUri), albumCoverUri = it.albumCoverUri, duration = it.duration, isFavourite = it.isFavourite)
    }

}