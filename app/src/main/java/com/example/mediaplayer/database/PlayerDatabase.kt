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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mediaplayer.model.SongEntity

@Database(entities = [FavouriteSongEntity::class, SongEntity::class], version = 7, exportSchema = false)
abstract class PlayerDatabase : RoomDatabase() {
    abstract fun favouriteSongsDao(): FavouriteSongsDao
    abstract fun songDao(): SongsDao

    companion object {

        @Volatile
        private var INSTANCE: PlayerDatabase? = null

        fun getInstance(context: Context): PlayerDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            PlayerDatabase::class.java,
                            "PlayerDatabase"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }


}

fun provideDatabase(context: Context) = PlayerDatabase.getInstance(context)



