package com.example.mediaplayer.ui.chosenSong

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.PlayerDestinations
import com.example.mediaplayer.databinding.ChosenSongFragmentBinding
import com.example.mediaplayer.ui.chosenSong.adapter.CenterZoomLayoutManager
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import com.example.mediaplayer.viewModels.ChosenSongViewModelFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class ChosenSongFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ChosenSongViewModelFactory

    private val viewModel by viewModels<ChosenSongViewModel> {
        val index: Int? = arguments?.getInt(CHOSEN_SONG_INDEX)
        val fromNotification = arguments?.getBoolean(PlayerDestinations.NOTIFICATION, false)
        viewModelFactory.apply {
            setData(index!!, fromNotification!!)
        }
    }

    private lateinit var songListAdapter: SongListAdapter
    private lateinit var imageListAdapter: ImageListAdapter

    private lateinit var binding: ChosenSongFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = ChosenSongFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpSongRecyclerView()
        setUpImageRecyclerView()
        setUpObserver()
    }

    private fun setUpObserver() {
        with(viewModel)
        {

            //restore the previous recycler view position after configuration changes
            if(viewModel.previousRecyclerViewPosition!=-1){
                setCurrentPositionRecyclerView(viewModel.previousRecyclerViewPosition,false)
            }
            chosenSongIndex.observe(viewLifecycleOwner, Observer { event ->
                event?.getContentIfNotHandled()?.let {
                    setCurrentPositionRecyclerView(it, true)
                    previousRecyclerViewPosition=it
                }
            })

        }
    }

    private fun setCurrentPositionRecyclerView(index: Int, scrollEnabled: Boolean) {
        (binding.playerLayout.listSong.adapter as SongListAdapter).setCurrentSelectedPosition(index, scrollEnabled)
        (binding.playerLayout.listImage.adapter as ImageListAdapter).setCurrentSelectedPosition(index, scrollEnabled)
    }

    private fun setUpSongRecyclerView() {
        songListAdapter = SongListAdapter(viewModel)
        binding.playerLayout.listSong.layoutManager = CenterZoomLayoutManager(context!!)
        binding.playerLayout.listSong.adapter = songListAdapter

    }

    private fun setUpImageRecyclerView() {
        imageListAdapter = ImageListAdapter(viewModel)
        binding.playerLayout.listImage.layoutManager = CenterZoomLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.playerLayout.listImage.adapter = imageListAdapter

    }

}// Required empty public constructor
