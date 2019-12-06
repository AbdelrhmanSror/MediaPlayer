package com.example.mediaplayer.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mediaplayer.repositry.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayListViewModel @Inject constructor(application: Application, repository: Repository) : AndroidViewModel(application) {

    val playLists = repository.observeSongs()
    private val _inProgress = MutableLiveData<Boolean?>()
    val inProgress: LiveData<Boolean?> = _inProgress


    init {

        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                Log.v("insertingdatabse", "inserting")
                _inProgress.postValue(true)
                repository.insertAudioIntoDatabase()
                _inProgress.postValue(null)
                Log.v("insertingdatabse", "finishing")


            }

        }
    }


}

