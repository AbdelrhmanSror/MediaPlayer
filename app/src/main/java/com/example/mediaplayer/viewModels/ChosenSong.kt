package com.example.mediaplayer.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.*
import com.example.mediaplayer.*
import com.example.mediaplayer.audioPlayer.OnPlayerStateChanged
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.repositry.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChosenSongViewModel(application: Application, private val songIndex: Int, private val fromNotification: Boolean) : AndroidViewModel(application), OnPlayerStateChanged {

    private val mApplication = application
    private val repository = Repository.getRepository(application)
    private lateinit var audioService: AudioForegroundService


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as AudioForegroundService.SongBinder
            audioService = binder.service
            audioService.registerObserver(this@ChosenSongViewModel, true, fromNotification)


        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            //nothing
        }
    }


    init {
        initializePlayList()
        //binding this fragment to service
        mApplication.bindService(Intent(mApplication, AudioForegroundService::class.java), connection, Context.BIND_AUTO_CREATE)

    }


    private fun initializePlayList() {
        viewModelScope.launch {
            val songPlaylist = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                repository.getSongs().toSongModel()
            }
            if (!fromNotification) {
                startForeground(songPlaylist as ArrayList<SongModel>, songIndex)
            }

        }
    }

    private fun startForeground(song: ArrayList<SongModel>, chosenSongIndex: Int) {
        val foregroundIntent = Intent(mApplication, AudioForegroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND.value
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, song)
        mApplication.startForeground(foregroundIntent)
    }


    val listOfSong = repository.observeSongs().map {
        it.toSongModel()
    }


    //get the list album art uris
    val imageCoverUris: LiveData<ArrayList<String?>> = listOfSong.map {
        val imageUris: ArrayList<String?> = ArrayList()
        for (item in it) {
            imageUris.add(item.albumCoverUri)
        }
        imageUris
    }

    private val _audioProgress = MutableLiveData<MutableLiveData<Long>>()
    var audioPlayerProgress: LiveData<Int> = _audioProgress.switchMap {
        it.map { progress ->
            (progress / 1000).toInt()
        }
    }

    //to observe when the current song track is changed
    private val _chosenSongIndex = MutableLiveData<Int?>()
    val chosenSongIndex: LiveData<Int?> = _chosenSongIndex

    // when service is initialized so we pass the live data to be observed and update the repeatMod,shuffleMode,playPauseButton Ui
    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> = _repeatMode

    private val _shuffleMode = MutableLiveData<Boolean>()
    val shuffleMode: LiveData<Boolean> = _shuffleMode

    private val _duration = MutableLiveData<Long?>()
    val duration: LiveData<Long?> = _duration

    private val _playPauseState = MutableLiveData<Event<Boolean>>()
    val playPauseState: LiveData<Event<Boolean>> = _playPauseState

    private val _playPauseStateInitial = MutableLiveData<Boolean?>()
    val playPauseStateInitial: LiveData<Boolean?> = _playPauseStateInitial


    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
        _playPauseStateInitial.value = isPlaying
        _chosenSongIndex.value = index

    }

    override fun onPlay() {
        _playPauseState.value = Event(true)

    }

    override fun onPause() {
        _playPauseState.value = Event(false)
    }

    override fun onShuffleModeChanged(enable: Boolean) {
        _shuffleMode.value = enable
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        _repeatMode.value = repeatMode
    }

    override fun onDurationChange(duration: Long) {
        _duration.value = duration
    }

    override fun onProgressChangedLiveData(progress: MutableLiveData<Long>) {
        _audioProgress.value = progress
    }

    fun setFavouriteAudio(chosenSongIndex: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                if (listOfSong.value!![chosenSongIndex].isFavourite) {
                    repository.removeFromFavouriteSongs(listOfSong.value!![chosenSongIndex].title)
                } else {
                    repository.addFavouriteSong(listOfSong.value!![chosenSongIndex])
                }

            }
        }
    }

    fun seekTo(index: Int) {

        audioService.seekTo(index)
    }

    fun seekToSecond(second: Int) {
        audioService.seekToSecond(second)
    }

    //for when user clicks on repeat and shuffle button
    fun onRepeatModeListener() {
        audioService.changeRepeatMode()
    }

    fun onShuffleModeListener() {
        audioService.changeShuffleMode()
    }

    fun onPlayPauseListener() {
        audioService.changeAudioState()
    }

    fun onPreviousListener() {
        audioService.goToPrevious()
    }

    fun onNextListener() {

        audioService.goToNext()
    }


    override fun onCleared() {
        super.onCleared()
        //un Bind fragment from service
        audioService.removeObserver(this, false)
        mApplication.unbindService(connection)
    }
}


class ChosenSongViewModelFactory(private val application: Application, private val chosenSongIndex: Int, private val fromNotification: Boolean) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChosenSongViewModel::class.java)) {
            return ChosenSongViewModel(application, chosenSongIndex, fromNotification) as T

        }
        throw IllegalArgumentException("unknown class")
    }

}