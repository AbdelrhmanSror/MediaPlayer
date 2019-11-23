package com.example.mediaplayer.ui.chosenSong

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.PlayerDestinations
import com.example.mediaplayer.databinding.ChosenSongFragmentBinding
import com.example.mediaplayer.ui.chosenSong.adapter.CenterZoomLayoutManager
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import com.example.mediaplayer.viewModels.ChosenSongViewModelFactory


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
        setUpSongRecyclerView()
        setUpImageRecyclerView()
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun setUpViewModel() {
        val index: Int? = arguments?.getInt(CHOSEN_SONG_INDEX)
        val fromNotification = arguments?.getBoolean(PlayerDestinations.NOTIFICATION.value, false)
        val factory = ChosenSongViewModelFactory(activity!!.application, index!!, fromNotification!!)
        viewModel = ViewModelProvider(this, factory).get(ChosenSongViewModel::class.java)

        with(viewModel)
        {
            chosenSongIndex.observe(viewLifecycleOwner, Observer { index ->
                index?.let {
                    (binding.playerLayout.listSong.adapter as SongListAdapter).setCurrentSelectedPosition(index)
                    (binding.playerLayout.listImage.adapter as ImageListAdapter).setCurrentSelectedPosition(index)
                }
            })
        }
    }


    private fun setUpSongRecyclerView() {
        val adapter = SongListAdapter(viewModel)
        binding.playerLayout.listSong.layoutManager = CenterZoomLayoutManager(context!!)
        //setup recyclerview with adapter
        binding.playerLayout.listSong.adapter = adapter

    }

    private fun setUpImageRecyclerView() {
        val adapter = ImageListAdapter(viewModel)
        val linearLayoutManager = CenterZoomLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.playerLayout.listImage.layoutManager = linearLayoutManager
        //setup recyclerview with adapter
        binding.playerLayout.listImage.adapter = adapter

    }


}// Required empty public constructor
