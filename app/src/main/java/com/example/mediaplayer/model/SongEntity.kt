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

package com.example.mediaplayer.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "songs"/*, indices = [Index(value = ["id"], unique = true)]*/)
data class SongEntity(
        @PrimaryKey
        val id: Long,
        var name: String,
        var actor: String,
        var audioUri: String,
        var albumCoverUri: String?,
        var duration: Long?)

fun List<SongEntity>.toSongModel(): List<SongModel> {
    return map {
        SongModel(id = it.id, title = it.name, artist = it.actor, audioUri = Uri.parse(it.audioUri), albumCoverUri = it.albumCoverUri, duration = it.duration)
    }

}

fun SongEntity.toSongModel(): SongModel {

    return SongModel(id = this.id, title = this.name, artist = this.actor, audioUri = Uri.parse(this.audioUri), albumCoverUri = this.albumCoverUri, duration = this.duration)


}