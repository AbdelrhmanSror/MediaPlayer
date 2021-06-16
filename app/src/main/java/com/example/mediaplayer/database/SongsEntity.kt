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

package com.example.mediaplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediaplayer.model.SongEntity


@Dao
interface SongsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(Songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(Songs: SongEntity)

    @Query("DELETE FROM songs WHERE id=:id")
    suspend fun deleteSong(id: Long)

    @Query("SELECT * FROM songs")
    fun getAllSongsLiveData(): LiveData<List<SongEntity>>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("DELETE FROM songs")
    suspend fun clear()
}
