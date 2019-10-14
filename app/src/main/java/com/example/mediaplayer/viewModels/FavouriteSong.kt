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

package com.example.mediaplayer.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayer.repositry.Repository


class FavouriteSongViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application) {
    val playLists = repository.getFavouriteSongs()

}

class FavouriteSongViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val repository: Repository = Repository(application)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavouriteSongViewModel::class.java)) {
            return FavouriteSongViewModel(application, repository) as T
        }
        throw IllegalArgumentException("unKnown class")
    }
}
