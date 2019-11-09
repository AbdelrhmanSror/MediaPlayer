package com.example.mediaplayer.ui.chosenSong

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.PlayerActions
import com.example.mediaplayer.R
import com.example.mediaplayer.audioPlayer.AudioPlayer
import com.example.mediaplayer.databinding.ChosenSongFragmentBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.twoDigitNumber
import com.example.mediaplayer.ui.OnItemClickListener
import com.example.mediaplayer.ui.chosenSong.adapter.CenterZoomLayoutManager
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import com.example.mediaplayer.viewModels.ChosenSongViewModelFactory
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
        val index: Int? = arguments?.getInt(CHOSEN_SONG_INDEX)
        val factory = ChosenSongViewModelFactory(activity!!.application, index!!)
        viewModel = ViewModelProvider(this, factory).get(ChosenSongViewModel::class.java)
        with(viewModel)
        {
            listSong.observe(viewLifecycleOwner, Observer {
                val songListAdapter = setUpSongRecyclerView(it)
                val imageListAdapter = setUpImageRecyclerView(imageCoverUris)
                //observe if the the the song was changed and based on that we reflect that change on ui
                chosenSongIndex.observe(viewLifecycleOwner, Observer { index ->
                    Log.v("chosenSongIndexChanged", "observerIndex $index")
                    songListAdapter.setCurrentSelectedPosition(index)
                    imageListAdapter.setCurrentSelectedPosition(index)
                    updateMediaSeekBarVal(audioService.audioPlayer)
                })

            })
        }
    }


    private fun setUpSongRecyclerView(listOfSong: List<SongModel>?): SongListAdapter {
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
            binding.playerLayout.listSong.layoutManager = CenterZoomLayoutManager(context!!)
            //setup recyclerview with adapter
            binding.playerLayout.listSong.adapter = adapter
            (binding.playerLayout.listSong.adapter as SongListAdapter).submitList(listOfSong)


        }
        return adapter
    }

    private fun setUpImageRecyclerView(imageUris: ArrayList<String?>): ImageListAdapter {
        var adapter = binding.root.playerLayout.list_image.adapter as ImageListAdapter?
        if (adapter == null) {
            val linearLayoutManager = CenterZoomLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
            /**creating adapter and set it with the recyclerview
            internally the adapter will focus this item and scroll to it if necessary*/
            adapter = ImageListAdapter(imageUris, object : OnItemClickListener {
                override fun onClick(itemClickIndex: Int) {
                    viewModel.seekTo(itemClickIndex)

                }
            })
            binding.root.playerLayout.list_image.layoutManager = linearLayoutManager
            //setup recyclerview with adapter
            binding.root.playerLayout.list_image.adapter = adapter


        }
        return adapter
    }

    private fun updateMediaSeekBarVal(player: AudioPlayer) {

        with(binding.playerController.mediaSeekBar) {
            player.AudioProgress().setOnProgressChanged(this@ChosenSongFragment) { duration, mProgress ->
                max = duration
                binding.playerController.duration.text = resources.getString(R.string.duration_format, (max / 60).twoDigitNumber(), (max % 60).twoDigitNumber())
                progress = mProgress
                //update the text position under seek bar to reflect the current position of seek bar
                binding.playerController.position.text = resources.getString(R.string.duration_format, (progress / 60).twoDigitNumber(), (progress % 60).twoDigitNumber())

            }

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, position: Int, fromUser: Boolean) {
                    if (fromUser) {
                        player.seekToSecond(position)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }
    }


}// Required empty public constructor
