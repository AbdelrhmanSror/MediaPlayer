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

package com.example.mediaplayer.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.mediaplayer.database.PlayerDatabase
import com.example.mediaplayer.repositry.Repository
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ApplicationModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideDataBase(application: Application): PlayerDatabase {
        return Room.databaseBuilder(application.applicationContext
                , PlayerDatabase::class.java, "SongList")
                .fallbackToDestructiveMigration()
                .build()
    }
}