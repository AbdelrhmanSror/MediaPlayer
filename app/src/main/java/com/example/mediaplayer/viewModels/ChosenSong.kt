package com.example.mediaplayer.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.foregroundService.ServiceAudioPlayerObserver
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.repositry.Repository
import com.example.mediaplayer.startForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChosenSongViewModel(application: Application, private val songIndex: Int, private val fromNotification: Boolean) : AndroidViewModel(application) {

    private val mApplication = application
    private val repository = Repository(application)

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as AudioForegroundService.SongBinder
            audioService = binder.service
            binder.service.setOnServiceAudioChangeListener(object : ServiceAudioPlayerObserver {
                override fun onAudioChanged(chosenSongIndex: Int, isPlaying: Boolean) {
                    _playPauseState.value = isPlaying to true
                    _chosenSongIndex.value = chosenSongIndex
                }

                override fun onPlay() {
                    Log.v("playPauseAnimation", "onPlay true")

                    _playPauseState.value = true to false

                }

                override fun onPause() {
                    Log.v("playPauseAnimation", "onPause false")

                    _playPauseState.value = false to false
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
            })


        }

        override fun onServiceDisconnected(arg0: ComponentName) {

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
                repository.getListOfSongs().toSongModel()
            }
            if (!fromNotification)
                startForeground(songPlaylist as ArrayList<SongModel>, songIndex)

        }
    }

    private fun startForeground(song: ArrayList<SongModel>, chosenSongIndex: Int) {
        val foregroundIntent = Intent(mApplication, AudioForegroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND.value
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, song)
        mApplication.startForeground(foregroundIntent)
    }


    val listSong = repository.getListOfSongsLivedata().map {
        it.toSongModel()
    }


    lateinit var audioService: AudioForegroundService


    //get the list album art uris
    val imageCoverUris: ArrayList<String?> by lazy {
        val imageUris: ArrayList<String?> = ArrayList()
        for (item in listSong.value!!) {
            imageUris.add(item.albumCoverUri)
        }
        imageUris
    }
    //to observe when the current song track is changed
    private val _chosenSongIndex = MutableLiveData<Int>()
    val chosenSongIndex: LiveData<Int> = _chosenSongIndex

    // when service is initialized so we pass the live data to be observed and update the repeatMod,shuffleMode,playPauseButton Ui
    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> = _repeatMode


    private val _shuffleMode = MutableLiveData<Boolean>()
    val shuffleMode: LiveData<Boolean> = _shuffleMode

    private val _duration = MutableLiveData<Long?>()
    val duration: LiveData<Long?> = _duration

    //live data of pair , first one indicating if the audio is playing or not ,the second one indicating
    // if its the initial state or not so if it was initial state we won't have to play animation
    private val _playPauseState = MutableLiveData<Pair<Boolean, Boolean>?>()
    val playPauseState: LiveData<Pair<Boolean, Boolean>?> = _playPauseState

    fun setFavouriteAudio(chosenSongIndex: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                if (listSong.value!![chosenSongIndex].isFavourite) {
                    repository.deleteFromFavouriteSongs(listSong.value!![chosenSongIndex].title)
                } else {
                    repository.insertIntoFavouriteSongs(listSong.value!![chosenSongIndex])
                }

            }
        }
    }

    fun seekTo(index: Int) {

        audioService.seekTo(index)
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