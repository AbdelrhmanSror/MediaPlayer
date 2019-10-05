package com.example.mediaplayer.viewModels

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.mediaplayer.AudioPlayer.AudioPlayer
import com.example.mediaplayer.R
import com.example.mediaplayer.foregroundService.AudioForgregroundService
import com.example.mediaplayer.model.PlayListModel
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.util.*

class ChosenSongViewModel(val playListModels: ArrayList<PlayListModel>?
                          , chosenSongIndex: Int, val fragmentPurpose: String?, application: Application) : AndroidViewModel(application) {
    private var audioPlayer: AudioPlayer? = null
    var isLikedTemp = false
    var isForegroundStarted: Boolean = false
    //to observe the changes with song index
    private val _chosenSongIndex = MutableLiveData<Int>()
    val chosenSongIndex: LiveData<Int>
        get() = _chosenSongIndex
    private val _chosenSongService = MutableLiveData<AudioForgregroundService>()
    val audioForegroundService: LiveData<AudioForgregroundService>
        get() = _chosenSongService
    //whenever the chosen song index change we update the song info to reflect the current index of song
    val songName = Transformations.map(_chosenSongIndex)
    {
        playListModels!![it].Title
    }
    val songActor = Transformations.map(_chosenSongIndex)
    {
        playListModels!![it].actor
    }
    val albumArtImage = Transformations.map(_chosenSongIndex)
    {
        playListModels!![it].albumCoverUri
    }
    val playerDuration: LiveData<Long> = Transformations.map(_chosenSongIndex)
    {
        playListModels!![_chosenSongIndex.value!!].duration
    }

    val repeatMode: LiveData<Boolean>
        get() = _repeatMode

    val shuffleMode: LiveData<Boolean>
        get() = _shuffleMode

    private val _repeatMode = MutableLiveData<Boolean>()
    private val _shuffleMode = MutableLiveData<Boolean>()


    private val _playPauseTriggered = MutableLiveData<Boolean>()
    val playPauseTriggered = Transformations.map(_playPauseTriggered)
    {
        if (it) {
            audioPlayer?.pause()
        } else {
            audioPlayer?.play()
        }
        _playPauseTriggered
    }
    private val _playPauseInitial = MutableLiveData<Drawable>()
    val playPauseInitial: LiveData<Drawable>
        get() = _playPauseInitial


    private val _favouriteTriggered = MutableLiveData<Boolean>()
    val favouriteTriggered: LiveData<Boolean>
        get() = _favouriteTriggered


    //for when user clicks on repeat and shuffle button
    fun repeatModeListener() {
        (audioPlayer as AudioPlayer).apply {
            repeatModeActivated = !audioPlayer!!.repeatModeActivated
            _repeatMode.value = repeatModeActivated
        }
    }

    fun shuffleModeListener() {
        (audioPlayer as AudioPlayer).apply {
            this.shuffleModeActivated = !this.shuffleModeActivated
            _shuffleMode.value = this.shuffleModeActivated
        }
    }

    fun playPauseListener() {
        _playPauseTriggered.value = audioPlayer!!.isPlaying
    }

    fun previousListener() {
        audioPlayer?.previous()
    }

    fun nextListener() {
        audioPlayer?.next()
    }

    fun addToFavouriteListener() {
        isLikedTemp = !isLikedTemp
        _favouriteTriggered.value = isLikedTemp
    }


    //set initial shape drawable to play pause button when first launched
    private fun playPauseInitial() {
        return if (audioPlayer!!.isPlaying) {
            _playPauseInitial.value = ContextCompat.getDrawable(getApplication(), R.drawable.pause_play_media)
        } else {
            _playPauseInitial.value = ContextCompat.getDrawable(getApplication(), R.drawable.play_pause_media)
        }
    }


    init {
        _chosenSongIndex.value = chosenSongIndex


    }

    fun setChosenSongService(service: AudioForgregroundService) {
        _chosenSongService.value = service
    }

    fun initializePlayer() {
        audioPlayer = _chosenSongService.value?.audioPlayer
        audioPlayer?.apply {
            _repeatMode.value = this.repeatModeActivated
            _shuffleMode.value = this.shuffleModeActivated
            playPauseInitial()

        }

        audioPlayer?.player?.apply {
            _chosenSongIndex.value = currentWindowIndex
            addListener(object : Player.EventListener {
                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    _chosenSongIndex.value = (audioPlayer as AudioPlayer).player.currentWindowIndex


                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode == Player.REPEAT_MODE_ALL
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleMode.value = shuffleModeEnabled

                }
            })

        }
    }


}

class ChosenSongViewModelFactory(private val playListModels: ArrayList<PlayListModel>?
                                 , private val chosenSongIndex: Int, private val fragmentPurpose: String?, private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ChosenSongViewModel::class.java)) {
            return ChosenSongViewModel(playListModels, chosenSongIndex, fragmentPurpose, application) as T

        }
        throw IllegalArgumentException("unknown class")
    }

}