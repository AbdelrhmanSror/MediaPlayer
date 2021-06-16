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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//creating view model factory
inline fun <reified T : ViewModelProvider.Factory> createViewModelFactory(application: Application): T? {
    return when (T::class.java) {
        ChosenSongViewModelFactory::class.java -> ChosenSongViewModelFactory(application) as T
        PlayListViewModelFactory::class.java -> PlayListViewModelFactory(application) as T
        FavouriteSongViewModelFactory::class.java -> FavouriteSongViewModelFactory(application) as T

        else -> null
    }

}

//creating view model
inline fun <reified T : ViewModel> createViewModel(context: Fragment, viewModelFactory: ViewModelProvider.Factory): T {
    return ViewModelProvider(context, viewModelFactory).get(T::class.java)
}
