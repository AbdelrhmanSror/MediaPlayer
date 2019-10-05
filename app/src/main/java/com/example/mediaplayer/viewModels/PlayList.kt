package com.example.mediaplayer.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.repositry.Repository
import kotlinx.coroutines.*

class PlayListViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val playLists = MutableLiveData<List<PlayListModel>?>()


    init {
        initPlaylistModels()
    }

    private fun initPlaylistModels() {
        uiScope.launch {
            playLists.value = getAudioList()
        }
    }

    private suspend fun getAudioList(): List<PlayListModel>? {
        return withContext(Dispatchers.IO)
        {
            repository.mediaData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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
