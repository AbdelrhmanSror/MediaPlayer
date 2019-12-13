package com.example.mediaplayer.viewModels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.audiofx.Visualizer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import com.example.mediaplayer.audioPlayer.AudioPlayerModel
import com.example.mediaplayer.audioPlayer.IPlayerState
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.repositry.Repository
import com.example.mediaplayer.shared.Event
import com.example.mediaplayer.shared.LIST_SONG
import com.example.mediaplayer.shared.PlayerActions
import com.example.mediaplayer.shared.startForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChosenSongViewModel @Inject constructor(application: Application,
                                              private val repository: Repository)
    : AndroidViewModel(application), IPlayerState<SongModel> {


    private val mApplication = application
    private lateinit var visualizer: Visualizer
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
        setUpForeground()
    }

    private fun setUpForeground() {
        startService()
        //binding this fragment to service
        mApplication.bindService(Intent(mApplication, AudioForegroundService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    private fun startService() {
        viewModelScope.launch {
            val songlist = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
                repository.getSongs().toSongModel()
            }
            //if (!fromNotification) {
            startForeground(songlist as ArrayList<SongModel>)
            //}
        }


    }

    private fun startForeground(song: ArrayList<SongModel>) {
        val foregroundIntent = Intent(mApplication, AudioForegroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, song)
        mApplication.startForeground(foregroundIntent)

    }

    override fun onAttached(audioPlayerModel: AudioPlayerModel?) {
        audioPlayerModel?.let {
            _playPauseStateInitial.value = it.isPlaying
            _chosenSongIndex.value = Event(it.currentIndex)
            _shuffleMode.value = it.shuffleModeEnabled
            _repeatMode.value = it.repeatMode
            _duration.value = it.duration

        }
    }

    override fun onAudioChanged(index: Int, isPlaying: Boolean, currentInstance: SongModel?) {
        _playPauseStateInitial.value = isPlaying
        _chosenSongIndex.value = Event(index)
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
        _audioPlayerProgress.value = progress
    }

    override fun onAudioSessionId(audioSessionId: Int) {
        setUpVisualizer(audioSessionId)
    }

    private fun setUpVisualizer(audioSessionId: Int) {
        //YOU NEED android.permission.RECORD_AUDIO for that in AndroidManifest.xml
        if (audioSessionId == 0) return
        visualizer = Visualizer(audioSessionId)
        visualizer.enabled = false
        visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
                _visualizerAnimationEnabled.value = p1


            }

            override fun onFftDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {}


        }, Visualizer.getMaxCaptureRate() / 2, true, false)

        visualizer.enabled = true
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

    /**
     * used by layout (data binding)
     */

    fun seekTo(index: Int) {
        Log.v("scrollingbehaviour", "seek to $index viewmodel")

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
        if (::visualizer.isInitialized)
            visualizer.release()
        //un Bind fragment from service
        audioService.removeObserver(this)
        mApplication.unbindService(connection)
    }
}
