package com.example.mediaplayer.ui.chosenSong

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.FRAGMENT_PURPOSE
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.databinding.FragmentChosenSongBinding
import com.example.mediaplayer.foregroundService.ChosenSongService
import com.example.mediaplayer.foregroundService.ChosenSongService.SongBinder
import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import com.example.mediaplayer.viewModels.ChosenSongViewModelFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.custom_controller.view.*
import kotlinx.android.synthetic.main.exo_player_view.view.*


data class MediaInfo(var playListModels: ArrayList<PlayListModel>? = arrayListOf(),
                     var chosenSongIndex: Int = 0, var fragmentPurpose: String? = PlayerActions.ACTION_FOREGROUND.value)

/**
 * A simple [Fragment] subclass.
 */
class ChosenSongFragment : Fragment() {

    private lateinit var viewModel: ChosenSongViewModel
    private lateinit var binding: FragmentChosenSongBinding
    private lateinit var foregroundIntent: Intent
    private lateinit var runnable: Runnable
    private val handler: Handler? = Handler()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentChosenSongBinding.inflate(inflater)
        setUpViewModel()
        startForeground()
        return binding.root
    }

    private fun getMediaInfo(): MediaInfo {
        arguments?.let {
            val playListModels: ArrayList<PlayListModel>? = arguments!!.getParcelableArrayList(LIST_SONG)
            val chosenSongIndex = arguments!!.getInt(CHOSEN_SONG_INDEX, 0)
            /**
             * the purpose of this variable is to decide if the intent is coming through notification or regular fragment
             * so if the action coming from notification we do not start foreground service because its already started
             * else we start it as usual
             */
            val fragmentPurpose = arguments!!.getString(FRAGMENT_PURPOSE)
            //clearing the bundle so when fragment stops no huge parcelable error occurs
            //avoid causing TransactionTooLargeException
            arguments!!.clear()
            return MediaInfo(playListModels, chosenSongIndex, fragmentPurpose)
        }

        return MediaInfo()
    }

    private fun setUpViewModel() {
        val info = getMediaInfo()
        val factory = ChosenSongViewModelFactory(info.playListModels, info.chosenSongIndex, info.fragmentPurpose)
        viewModel = ViewModelProviders.of(this, factory).get(ChosenSongViewModel::class.java)
        with(viewModel)
        {
            //observe if the service is yet initialized or not  so we can synchronize it with ui
            chosenSongService.observe(viewLifecycleOwner, Observer { service ->
                service?.let {
                    binding.playerView.initializePlayer(service)
                    //observe if the the the song was changed and based on that we reflect that change on ui
                    chosenSongIndex.observe(viewLifecycleOwner, Observer { index ->
                        // will be called and  then this live data also will be called responding to onSeekProcessed so we avoid looping forever by this if statement
                        binding.playerView.media_seek_bar.updateMediaSeekBarVal(service.audioPlayer.player)
                        //whenever the chosen song index change we update the song info to reflect the current index of song
                        binding.playerView.songDetails(index)
                    })

                }

            })
        }
    }


    private fun PlayerView.songDetails(index: Int) {
        this.songName.text = viewModel.playListModels!![index].Title
        this.songActor.text = viewModel.playListModels!![index].actor


    }

    private fun SeekBar.updateMediaSeekBarVal(player: SimpleExoPlayer) {
        runnable = Runnable {
            with(player)
            {
                max = (duration / 1000).toInt()
                progress = (currentPosition / 1000).toInt()
                handler?.postDelayed(runnable, 50)
            }
        }
        handler?.postDelayed(runnable, 0)
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, position: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(position * 1000.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }


    private fun PlayerView.initializePlayer(service: ChosenSongService) {
        val audioPlayer = service.audioPlayer
        //Attaching the player to a view
        this.player = audioPlayer.player
        this.useController = true
        this.showController()
        this.controllerAutoShow = true
        viewModel.setChosenSongIndex(player.currentWindowIndex)
        audioPlayer.player.addListener(object : Player.EventListener {
            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                viewModel.setChosenSongIndex(player.currentWindowIndex)

            }
        })


    }


    override fun onStart() {
        super.onStart()
        //binding this fragment to service
        activity!!.bindService(foregroundIntent, connection, Context.BIND_AUTO_CREATE)


    }

    override fun onPause() {
        super.onPause()
        if (::runnable.isInitialized)
            handler?.removeCallbacks(runnable)

    }

    override fun onStop() {
        super.onStop()
        //un Bind fragment from service
        activity!!.unbindService(connection)

    }


    private fun startForeground() {
        foregroundIntent = Intent(activity, ChosenSongService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND.value
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, viewModel.chosenSongIndex.value)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, viewModel.playListModels)
        // if the purpose fragment is coming from notification then the service is already started ,no need to start it again
        if (viewModel.fragmentPurpose != PlayerActions.AUDIO_FOREGROUND_NOTIFICATION.value) {
            //Start service:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity!!.startForegroundService(foregroundIntent)

            } else {
                activity!!.startService(foregroundIntent)

            }
        }

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as SongBinder
            viewModel.setChosenSongService(binder.service)

        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }

}// Required empty public constructor
