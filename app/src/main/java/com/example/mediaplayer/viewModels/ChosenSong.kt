package com.example.mediaplayer.viewModels

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.mediaplayer.R
import com.example.mediaplayer.database.SongEntity
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.repositry.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChosenSongViewModel(application: Application) : AndroidViewModel(application) {


    private val repository = Repository(application)

    private val _audioService = MutableLiveData<AudioForegroundService>()

    val audioService: LiveData<AudioForegroundService>
        get() = _audioService

    val listOfSong = Transformations.switchMap(_audioService) {
        _audioService.value!!.listOfSong
    }

    //get the list album art uris
    val imageCoverUris: ArrayList<String?> by lazy {
        val imageUris: ArrayList<String?> = ArrayList()
        for (item in _audioService.value!!.listOfSong.value!!) {
            imageUris.add(item.albumCoverUri)
        }
        imageUris
    }
    //to observe when the current song track is changed
    val chosenSongIndex = Transformations.switchMap(_audioService)
    {
        _audioService.value!!.trackChanged
    }

    val playerDuration: LiveData<Long> = Transformations.map(chosenSongIndex)
    {
        _audioService.value!!.listOfSong.value!![it].duration
    }

    // when service is initialized so we pass the live data to be observed and update the repeatMod,shuffleMode,playPauseButton Ui
    val repeatMode = Transformations.switchMap(_audioService) {
        audioService.value!!.repeatModeChanged
    }

    val shuffleMode = Transformations.switchMap(_audioService) {
        audioService.value!!.shuffleModeChanged
    }

    val playPauseAnimation = Transformations.switchMap(_audioService) {
        _audioService.value!!.playerStateChanged
    }

    private val _playPauseInitial = MutableLiveData<Drawable>()
    val playPauseInitial: LiveData<Drawable>
        get() = _playPauseInitial

    private val _listSong = MutableLiveData<List<SongEntity>>()
    val listSong: LiveData<List<SongEntity>>
        get() = _listSong
    //set initial shape drawable to play pause button when first launched
    private fun playPauseInitial() {
        return if (isAudioPlaying()) {
            _playPauseInitial.value = ContextCompat.getDrawable(getApplication(), R.drawable.pause_play_media)
        } else {
            _playPauseInitial.value = ContextCompat.getDrawable(getApplication(), R.drawable.play_pause_media)
        }
    }

    fun setFavouriteAudio(chosenSongIndex: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                if (listOfSong.value!![chosenSongIndex].isFavourite) {
                    repository.deleteFromFavouriteSongs(listOfSong.value!![chosenSongIndex].title)
                } else {
                    repository.insertIntoFavouriteSongs(listOfSong.value!![chosenSongIndex])
                }
                //updating the specific element of list of song in foreground service so the update can be reflected to our ui
                //because foreground service is considered as source of listOfSong
                _audioService.value!!.updateElementOfListOfSong(chosenSongIndex)
            }
        }
    }


    private fun changeRepeatMode() {
        _audioService.value!!.changeRepeatMode()
    }

    private fun changeShuffleMode() {
        _audioService.value!!.changeShuffleMode()
    }

    private fun changeAudioState() {
        _audioService.value!!.changeAudioState()

    }

    private fun goToPrevious() {
        _audioService.value!!.goToPrevious()
    }

    private fun goToNext() {
        _audioService.value!!.goToNext()
    }

    fun seekTo(index: Int) {
        _audioService.value!!.seekTo(index)
    }

    private fun isAudioPlaying(): Boolean {
        return _audioService.value!!.isAudioPlaying()
    }


    //for when user clicks on repeat and shuffle button
    fun repeatModeListener() {
        changeRepeatMode()
    }

    fun shuffleModeListener() {
        changeShuffleMode()
    }

    fun playPauseListener() {
        changeAudioState()
    }

    fun previousListener() {
        goToPrevious()
    }

    fun nextListener() {
        goToNext()
    }


    fun setChosenSongService(service: AudioForegroundService) {
        _audioService.value = service
    }

    fun initializePlayer() {
        playPauseInitial()


    }

}

class ChosenSongViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChosenSongViewModel::class.java)) {
            return ChosenSongViewModel(application) as T

        }
        throw IllegalArgumentException("unknown class")
    }

}