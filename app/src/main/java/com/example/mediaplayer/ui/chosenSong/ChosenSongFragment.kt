package com.example.mediaplayer.ui.chosenSong

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.databinding.ChosenSongFragmentBinding
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.foregroundService.AudioForegroundService.SongBinder
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.ui.OnItemClickListener
import com.example.mediaplayer.ui.chosenSong.adapter.CenterZoomLayoutManager
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import com.example.mediaplayer.viewModels.ChosenSongViewModelFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.chosen_song_fragment.view.*
import kotlinx.android.synthetic.main.fragment_favourite.view.list_song
import kotlinx.android.synthetic.main.player_layout.view.*


data class MediaInfo(var songList: ArrayList<SongModel>? = arrayListOf(),
                     var chosenSongIndex: Int = 0, var fragmentPurpose: String = PlayerActions.ACTION_FOREGROUND.value)

/**
 * A simple [Fragment] subclass.
 */
class ChosenSongFragment : Fragment() {

    private lateinit var viewModel: ChosenSongViewModel
    private lateinit var binding: ChosenSongFragmentBinding
    private lateinit var mRunnable: Runnable
    private val mHandler: Handler? = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = ChosenSongFragmentBinding.inflate(inflater)
        setUpViewModel()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun setUpViewModel() {
        val factory = ChosenSongViewModelFactory(activity!!.application)
        viewModel = ViewModelProviders.of(this, factory).get(ChosenSongViewModel::class.java)
        with(viewModel)
        {
            listSong.observe(this@ChosenSongFragment, Observer {
                Log.v("serviceIsCalled", "again")

            })
            //observe if the service is yet initialized or not  so we can synchronize it with ui
            audioService.observe(viewLifecycleOwner, Observer { service ->
                service?.let {
                    //observe if the the the song was changed and based on that we reflect that change on ui
                    chosenSongIndex.observe(viewLifecycleOwner, Observer {
                        setUpSongRecyclerView()
                        setUpImageRecyclerView()
                        listOfSong.observe(viewLifecycleOwner, Observer {
                            (binding.root.playerLayout.list_song.adapter as SongListAdapter).submitList(it)
                        })
                        viewModel.initializePlayer()
                        // will be called and  then this live data also will be called responding to onSeekProcessed so we avoid looping forever by this if statement
                        updateMediaSeekBarVal(service.audioPlayer.player!!)
                    })

                }

            })
        }
    }


    private fun setUpSongRecyclerView(): SongListAdapter {
        var adapter = binding.root.playerLayout.list_song.adapter as SongListAdapter?
        if (adapter == null) {
            /**creating adapter and set it with the recycler view
            when user clicks on item in recycler view it will play the audio with that index
            and internally the adapter will focus this item and scroll to it if necessary*/
            adapter = SongListAdapter(object : OnItemClickListener {
                override fun onClick(itemClickIndex: Int) {
                    viewModel.seekTo(itemClickIndex)

                }

                override fun onFavouriteClick(itemClickIndex: Int) {
                    //updating the database to reverse variable is favourite of current song
                    viewModel.setFavouriteAudio(itemClickIndex)
                }
            })
            binding.root.playerLayout.list_song.layoutManager = CenterZoomLayoutManager(context!!)
            //setup recyclerview with adapter
            binding.root.playerLayout.list_song.adapter = adapter

            //select the current focused position to focus and scroll to
            adapter.setCurrentSelectedPosition(viewModel.chosenSongIndex.value!!)


        } else {
            //update the adapter to reflect the current selected song
            adapter.setCurrentSelectedPosition(viewModel.chosenSongIndex.value!!)
        }
        return adapter
    }

    private fun setUpImageRecyclerView(): ImageListAdapter {
        var adapter = binding.root.playerLayout.list_image.adapter as ImageListAdapter?
        if (adapter == null) {
            val linearLayoutManager = CenterZoomLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
            /**creating adapter and set it with the recyclerview
            internally the adapter will focus this item and scroll to it if necessary*/
            adapter = ImageListAdapter(viewModel.imageCoverUris, object : OnItemClickListener {
                override fun onClick(itemClickIndex: Int) {
                    Log.v("listOfSong", "clciked")
                    viewModel.seekTo(itemClickIndex)

                }
            })
            binding.root.playerLayout.list_image.layoutManager = linearLayoutManager
            //setup recyclerview with adapter
            binding.root.playerLayout.list_image.adapter = adapter
            //select the current focused position to focus and scroll to
            adapter.setCurrentSelectedPosition(viewModel.chosenSongIndex.value!!)


        } else {
            //update the adapter to reflect the current selected song
            adapter.setCurrentSelectedPosition(viewModel.chosenSongIndex.value!!)
        }
        return adapter
    }

    private fun updateMediaSeekBarVal(player: SimpleExoPlayer) {
        with(binding.playerController.mediaSeekBar) {
            mRunnable = Runnable {
                with(player)
                {
                    max = (duration / 1000).toInt()
                    progress = (currentPosition / 1000).toInt()
                    mHandler?.postDelayed(mRunnable, 50)
                }
            }
            handler?.postDelayed(mRunnable, 0)
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
    }


    override fun onStart() {
        super.onStart()
        //binding this fragment to service
        activity!!.bindService(Intent(activity, AudioForegroundService::class.java), connection, Context.BIND_AUTO_CREATE)


    }

    override fun onPause() {
        super.onPause()
        if (::mRunnable.isInitialized)
            mHandler?.removeCallbacks(mRunnable)

    }

    override fun onStop() {
        super.onStop()
        //un Bind fragment from service
        activity!!.unbindService(connection)

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
