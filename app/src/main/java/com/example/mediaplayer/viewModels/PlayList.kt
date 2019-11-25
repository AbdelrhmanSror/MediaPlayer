package com.example.mediaplayer.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mediaplayer.repositry.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayListViewModel @Inject constructor(application: Application, repository: Repository) : AndroidViewModel(application) {

    val playLists = repository.observeSongs()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                Log.v("insertingdatabse","inserting")
                repository.insertAudioIntoDatabase()
            }

        }
    }


}

