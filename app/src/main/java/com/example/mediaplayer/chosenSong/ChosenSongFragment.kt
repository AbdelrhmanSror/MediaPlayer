package com.example.mediaplayer.chosenSong

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mediaplayer.chosenSong.ChosenSongService.SongBinder
import com.example.mediaplayer.constants.*
import com.example.mediaplayer.databinding.FragmentChosenSongBinding
import com.example.mediaplayer.playlist.PlayListModel
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.exo_player_view.view.*
import java.util.*


data class MediaInfo(var playListModels: ArrayList<PlayListModel>? = null,
                     var chosenSongIndex: Int = 0, val purposeOfFragment: String = ACTION_PLAYING_AUDIO_FOREGROUND)

/**
 * A simple [Fragment] subclass.
 */
class ChosenSongFragment : Fragment() {

    private lateinit var viewModel: ChosenSongViewModel
    private lateinit var binding: FragmentChosenSongBinding
    private lateinit var foregroundIntent: Intent


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentChosenSongBinding.inflate(inflater)
        setUpViewModel()
        startForeground()
        return binding.root
    }

    private fun getMediaInfo(): MediaInfo {
        if (arguments != null) {
            val playListModels: ArrayList<PlayListModel>? = arguments!!.getParcelableArrayList(LIST_SONG)
            val chosenSongIndex = arguments!!.getInt(CHOSEN_SONG_INDEX, 0)
            val purposeOfFragment = arguments!!.getString(AUDIO_FOREGROUND_NOTIFICATION, ACTION_PLAYING_AUDIO_FOREGROUND)
            //clearing the bundle so when fragment stops no huge parcelable error occurs
            //avoid causing TransactionTooLargeException
            arguments!!.clear()
            return MediaInfo(playListModels, chosenSongIndex, purposeOfFragment)
        }
        return MediaInfo()
    }

    private fun setUpViewModel() {
        val info = getMediaInfo()
        val factory = ChosenSongViewModelFactory(info.playListModels, info.chosenSongIndex, info.purposeOfFragment)
        viewModel = ViewModelProviders.of(this, factory).get(ChosenSongViewModel::class.java)

        //observe if the service is yet initialized or not  so we can setup player and synchronize it with ui
        viewModel.chosenSongService.observe(viewLifecycleOwner, Observer { service ->
            service?.let {
                binding.playerView.initializePlayer(service)
                //if this fragment is opened through service then the source of truth is service and set the value of chosenSongIndex from service
                if (viewModel.purposeOfFragment == AUDIO_FOREGROUND_NOTIFICATION) {
                    viewModel.setChosenSongIndex(service.player.currentWindowIndex)
                }
                //observe if the the the song was changed and based on that we reflect that change on ui
                viewModel.chosenSongIndex.observe(viewLifecycleOwner, Observer { index ->
                    index.let {
                        //to avoid setup player again when configuration changes happen or if player fragment has opened  from notification
                        if (!viewModel.isServiceCreatedBefore && viewModel.purposeOfFragment != AUDIO_FOREGROUND_NOTIFICATION)
                            viewModel.playListModels?.let {
                                service.setUpPlayer(it, index)
                            }
                        //whenever the chosen song index change we update the song info to reflect the current index of song
                        binding.playerView.songDetails(index)
                    }
                })
                //set this service as created before so we do not setupPlayer again
                viewModel.isServiceCreatedBefore = true

            }

        })
    }


    private fun PlayerView.songDetails(index: Int) {
        this.songName.text = viewModel.playListModels!![index].title
        this.songActor.text = viewModel.playListModels!![index].actor


    }


    private fun PlayerView.initializePlayer(service: ChosenSongService) {
        val player = service.player
        //Attaching the player to a view
        this.player = player
        this.useController = true
        this.showController()
        this.controllerAutoShow = true
        service.player.addListener(object : EventListener {

            override fun onSeekProcessed() {
                viewModel.setChosenSongIndex(service.player.currentWindowIndex)
            }

        })


    }


    override fun onStart() {
        super.onStart()
        //binding this fragment to service
        Objects.requireNonNull<FragmentActivity>(activity).bindService(foregroundIntent, connection, Context.BIND_AUTO_CREATE)


    }

    override fun onStop() {
        super.onStop()
        //un Bind fragment from service
        Objects.requireNonNull<FragmentActivity>(activity).unbindService(connection)

    }


    private fun startForeground() {
        foregroundIntent = Intent(activity, ChosenSongService::class.java)
        foregroundIntent.action = ACTION_FOREGROUND
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, viewModel.chosenSongIndex.value)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, viewModel.playListModels)
        //Start service:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull<FragmentActivity>(activity).startForegroundService(foregroundIntent)

        } else {
            Objects.requireNonNull<FragmentActivity>(activity).startService(foregroundIntent)

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
            val mService = binder.service
            viewModel.setChosenSongService(mService)

        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }

}// Required empty public constructor
