package com.example.mediaplayer.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
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
import javax.inject.Inject

class ChosenSongViewModel(application: Application,
                          private val repository: Repository,
                          private val songIndex: Int,
                          private val fromNotification: Boolean)
    : AndroidViewModel(application), OnPlayerStateChanged {


    private val mApplication = application

    private lateinit var audioService: AudioForegroundService

    var previousRecyclerViewPosition = -1

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

    //to observe when the current song track is changed
    private val _chosenSongIndex = MutableLiveData<Event<Int>>()
    val chosenSongIndex: LiveData<Event<Int>> = _chosenSongIndex

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

    private val _audioPlayerProgress = MutableLiveData<MutableLiveData<Long>>()
    var audioPlayerProgress: LiveData<Int> = _audioPlayerProgress.switchMap {
        it.map { progress ->
            (progress / 1000).toInt()
        }
    }

    private val _equalizerAnimationEnabled = MutableLiveData<Boolean?>()
    val equalizerAnimationEnabled: LiveData<Boolean?> = _equalizerAnimationEnabled
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as AudioForegroundService.SongBinder
            audioService = binder.service
            audioService.registerObserver(this@ChosenSongViewModel, instantTrigger = fromNotification)


        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            //nothing
            Log.v("playpausestate", "servicedisconnected")

        }
    }

    init {
        startService()
        //binding this fragment to service
        mApplication.bindService(Intent(mApplication, AudioForegroundService::class.java), connection, Context.BIND_AUTO_CREATE)
    }


    private fun startService() {
        viewModelScope.launch {
            val songlist = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                repository.getSongs().toSongModel()
            }
            if (!fromNotification) {
                startForeground(songlist as ArrayList<SongModel>, songIndex)
            }
        }


    }

    private fun startForeground(song: ArrayList<SongModel>, chosenSongIndex: Int) {
        val foregroundIntent = Intent(mApplication, AudioForegroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, song)
        mApplication.startForeground(foregroundIntent)
    }


    override fun onAudioChanged(index: Int, isPlaying: Boolean) {
        _equalizerAnimationEnabled.value = isPlaying
        _playPauseStateInitial.value = isPlaying
        _chosenSongIndex.value = Event(index)

    }

    override fun onPlay() {
        _equalizerAnimationEnabled.value = true
        _playPauseState.value = Event(true)

    }

    override fun onPause() {
        _equalizerAnimationEnabled.value = false
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
        _audioPlayerProgress.value = progress
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
        audioService.removeObserver(this)
        mApplication.unbindService(connection)
    }
}

class ChosenSongViewModelFactory @Inject constructor(private val application: Application, private val repository: Repository) : ViewModelProvider.Factory {

    private var chosenSongIndex: Int? = null
    private var fromNotification: Boolean? = null
    fun setData(songIndex: Int, fromNotification: Boolean) {
        chosenSongIndex = songIndex
        this.fromNotification = fromNotification

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChosenSongViewModel::class.java)) {
            return ChosenSongViewModel(application, repository, chosenSongIndex!!, fromNotification!!) as T

        }
        throw IllegalArgumentException("unknown class")
    }

}