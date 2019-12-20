package com.example.mediaplayer.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.audiofx.Visualizer
import android.os.IBinder
import androidx.lifecycle.*
import com.example.mediaplayer.audioPlayer.AudioPlayerModel
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.extensions.startForeground
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toSongEntity
import com.example.mediaplayer.repositry.Repository
import com.example.mediaplayer.shared.CHOSEN_SONG_INDEX
import com.example.mediaplayer.shared.Event
import com.example.mediaplayer.shared.LIST_SONG
import com.example.mediaplayer.shared.PlayerActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChosenSongViewModel(application: Application,
                          private val repository: Repository,
                          private val songIndex: Int,
                          private val fromNotification: Boolean)
    : AndroidViewModel(application), IPlayerState {


    private val mApplication = application

    private var visualizer: Visualizer? = null
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
    private var msonglist: List<SongModel>? = null
    private var favouriteSongList: ArrayList<SongModel> = ArrayList()
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

    private val _visualizerAnimationEnabled = MutableLiveData<ByteArray?>()
    val visualizerAnimationEnabled: LiveData<ByteArray?> = _visualizerAnimationEnabled


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as AudioForegroundService.SongBinder
            audioService = binder.service
            audioService.registerObserver(this@ChosenSongViewModel)

        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            //nothing
        }
    }

    init {
        startService()
        //binding this fragment to service
        mApplication.bindService(Intent(mApplication, AudioForegroundService::class.java), connection, Context.BIND_AUTO_CREATE)
    }


    private fun startService() {
        viewModelScope.launch {
            val songlist: List<SongModel> = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                repository.getSongs().toSongModel()
            }
            msonglist = songlist
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

    override fun onAttached(audioPlayerModel: AudioPlayerModel?) {
        audioPlayerModel?.let {
            _playPauseStateInitial.postValue(it.isPlaying)
            _chosenSongIndex.postValue(Event(it.currentIndex))
            _shuffleMode.value = it.shuffleModeEnabled
            _repeatMode.value = it.repeatMode
            _duration.postValue(it.duration)

        }
    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean, currentInstance: Any?) {
        _playPauseStateInitial.postValue(isPlaying)
        _chosenSongIndex.postValue(Event(index))
    }


    override fun onPlay() {
        _playPauseState.postValue(Event(true))

    }

    override fun onPause() {
        _playPauseState.postValue(Event(false))
    }

    override fun onShuffleModeChanged(enable: Boolean) {
        _shuffleMode.value = enable
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        _repeatMode.value = repeatMode
    }

    override fun onDurationChange(duration: Long) {
        _duration.postValue(duration)
    }

    override fun onProgressChangedLiveData(progress: MutableLiveData<Long>) {
        _audioPlayerProgress.postValue(progress)
    }

    override fun onAudioSessionId(audioSessionId: Int) {
        setUpVisualizer(audioSessionId)
    }

    private fun setUpVisualizer(audioSessionId: Int) {
        //YOU NEED android.permission.RECORD_AUDIO for that in AndroidManifest.xml
        if (audioSessionId == 0) return
        visualizer?.release()
        visualizer = Visualizer(audioSessionId)
        visualizer?.enabled = false
        visualizer?.captureSize = Visualizer.getCaptureSizeRange()[1]
        visualizer?.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
                _visualizerAnimationEnabled.value = p1

            }

            override fun onFftDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {}


        }, Visualizer.getMaxCaptureRate() / 2, true, false)

        visualizer?.enabled = true
    }


    /*  fun setFavouriteAudio(chosenSongIndex: Int) {
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
       }*/
    fun setFavouriteAudio(chosenSongIndex: Int) {
        msonglist?.let {
            it[chosenSongIndex].isFavourite = !it[chosenSongIndex].isFavourite
            updateFavouriteSongs(it[chosenSongIndex].isFavourite, it[chosenSongIndex])

        }
    }

    //if true then add else remove
    private fun updateFavouriteSongs(addOrRemove: Boolean, songModel: SongModel) {
        if (!addOrRemove) {
            repository.removeFromFavouriteSongs(songModel.title)
        } else {
            repository.addFavouriteSong(songModel)
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
        visualizer?.release()
        repository.insertSongs(favouriteSongList.toSongEntity())
        //un Bind fragment from service
        audioService.removeObserver(this)
        mApplication.unbindService(connection)
    }
}

class ChosenSongViewModelFactory @Inject constructor(private val application: Application,
                                                     private val repository: Repository)
    : ViewModelProvider.Factory {

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