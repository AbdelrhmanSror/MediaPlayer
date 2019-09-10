package com.example.mediaplayer.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayer.foregroundService.ChosenSongService
import com.example.mediaplayer.model.PlayListModel
import java.util.*


class ChosenSongViewModel(val playListModels: ArrayList<PlayListModel>?
                          , chosenSongIndex: Int) : ViewModel() {
    //to observe the changes with song index
    private val _chosenSongIndex = MutableLiveData<Int>()
    val chosenSongIndex: LiveData<Int>
        get() = _chosenSongIndex

    private val _chosenSongService = MutableLiveData<ChosenSongService>()
    val chosenSongService: LiveData<ChosenSongService>
        get() = _chosenSongService

    init {
        _chosenSongIndex.value = chosenSongIndex

    }

    fun setChosenSongIndex(chosenSongIndex: Int) {
        _chosenSongIndex.value = chosenSongIndex
    }

    fun setChosenSongService(service: ChosenSongService) {
        _chosenSongService.value = service
    }

}

class ChosenSongViewModelFactory(private val playListModels: ArrayList<PlayListModel>?
                                 , private val chosenSongIndex: Int) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChosenSongViewModel::class.java)) {
            return ChosenSongViewModel(playListModels, chosenSongIndex) as T

        }
        throw IllegalArgumentException("unknown class")
    }

}