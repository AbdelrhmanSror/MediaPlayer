package com.example.mediaplayer.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.repositry.Repository

class PlayListViewModel(application: Application, repository: Repository) : AndroidViewModel(application) {
    val playLists: List<PlayListModel>? = repository.mediaData


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
