package com.example.mediaplayer.chosenSong

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mediaplayer.playlist.PlayListModel
import java.util.*

internal class ChosenSongViewModel(val playListModels: ArrayList<PlayListModel>?
                                   , chosenSongIndex: Int, val purposeOfFragment: String?) : ViewModel() {
    //to observe the changes with song index
    private var _chosenSongIndex = MutableLiveData<Int>()
    val chosenSongIndex: LiveData<Int>
        get() = _chosenSongIndex

    private var _chosenSongService = MutableLiveData<ChosenSongService>()
    val chosenSongService: LiveData<ChosenSongService>
        get() = _chosenSongService

    var isServiceCreatedBefore = false

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
