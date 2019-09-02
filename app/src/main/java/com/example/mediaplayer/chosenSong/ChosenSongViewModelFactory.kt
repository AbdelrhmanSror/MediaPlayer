package com.example.mediaplayer.chosenSong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mediaplayer.playlist.PlayListModel
import java.util.*

class ChosenSongViewModelFactory(private val playListModels: ArrayList<PlayListModel>?
                                 , private val chosenSongIndex: Int
                                 , private val purposeOfFragment: String?) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChosenSongViewModel::class.java)) {
            return ChosenSongViewModel(playListModels, chosenSongIndex, purposeOfFragment) as T

        }
        throw IllegalArgumentException("unknown class")
    }
}
