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

package com.example.mediaplayer.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.mediaplayer.model.SongEntity
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.repositry.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayListViewModel constructor(application: Application, private val tracksRepository: TracksRepository) : AndroidViewModel(application) {

    val playLists: LiveData<List<SongEntity>> = tracksRepository.observeSongs()
    private val _inProgress = MutableLiveData<Boolean?>()
    val inProgress: LiveData<Boolean?> = _inProgress


    init {

        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                Log.v("updatinglistagin", "done")
                _inProgress.postValue(true)
                tracksRepository.insertAudioIntoDatabase()
                _inProgress.postValue(null)

            }

        }
    }

    fun addToFavourite(songModel: SongModel) {
        tracksRepository.addFavouriteSong(songModel)

    }

}

class PlayListViewModelFactory constructor(private val application: Application)
    : ViewModelProvider.Factory {
    private var tracksRepository: TracksRepository? = null

    fun setData(tracksRepository: TracksRepository) {
        this.tracksRepository = tracksRepository

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(PlayListViewModel::class.java)) {
            return PlayListViewModel(application, tracksRepository!!) as T

        }
        throw IllegalArgumentException("unknown class")
    }

}


