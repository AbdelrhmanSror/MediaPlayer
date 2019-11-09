package com.example.mediaplayer.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mediaplayer.repositry.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayListViewModel(application: Application, repository: Repository) : AndroidViewModel(application) {

    val playLists = repository.getListOfSongsLivedata()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                repository.insertAudioIntoDatabase()
            }

        }
    }


}

class PlayListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val repository: Repository = Repository(application)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayListViewModel::class.java)) {
            return PlayListViewModel(application, repository) as T
        }
        throw IllegalArgumentException("unKnown class")
    }
}
