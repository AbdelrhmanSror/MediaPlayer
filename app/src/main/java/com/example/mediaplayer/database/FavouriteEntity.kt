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
import androidx.room.*
import com.example.mediaplayer.model.SongEntity


@Entity(tableName = "favouriteSongs", indices = [Index(value = ["songId"], unique = true)])
data class FavouriteSongEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val songId: Long, var name: String)

@Dao
interface FavouriteSongsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFavouriteSong(favouriteSong: FavouriteSongEntity)

    @Query("DELETE FROM favouriteSongs WHERE songId=:id")
    fun deleteFavouriteSong(id: Long)

    @Query("SELECT * FROM songs INNER JOIN favouriteSongs ON songs.id=favouriteSongs.songId")
    fun getAllFavouriteSong(): LiveData<List<SongEntity>>

    @Query("DELETE FROM favouriteSongs")
    fun clear()
}
