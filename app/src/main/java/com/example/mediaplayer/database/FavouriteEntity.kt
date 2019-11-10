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


@Entity(tableName = "favouriteSongs", indices = [Index(value = ["name"], unique = true)], foreignKeys = [ForeignKey(
        entity = SongEntity::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("name"))])
data class FavouriteSongEntity(@PrimaryKey var name: String,
                               var actor: String,
                               var audioUri: String,
                               var albumCoverUri: String?,
                               var duration: Long)

@Dao
interface FavouriteSongsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFavouriteSong(favouriteSong: FavouriteSongEntity)

    @Query("DELETE FROM favouriteSongs WHERE name=:name")
    fun deleteFavouriteSong(name: String)

    @Query("SELECT * FROM favouriteSongs")
    fun getAllFavouriteSong(): LiveData<List<FavouriteSongEntity>>

    @Query("DELETE FROM favouriteSongs")
    fun clear()
}

fun List<FavouriteSongEntity>.toSongModel(): List<SongModel> {
    return map {
        SongModel(title = it.name, actor = it.actor, audioUri = Uri.parse(it.audioUri), albumCoverUri = it.albumCoverUri, duration = it.duration, isFavourite = true)
    }

}

fun FavouriteSongEntity.toSongModel(): SongModel {
    return SongModel(title = this.name, actor = this.actor, audioUri = Uri.parse(this.audioUri), albumCoverUri = this.albumCoverUri, duration = this.duration, isFavourite = true)


}