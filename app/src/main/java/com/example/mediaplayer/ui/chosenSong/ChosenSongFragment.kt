/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.ui.chosenSong

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediaplayer.database.provideDatabase
import com.example.mediaplayer.databinding.ChosenSongFragmentBinding
import com.example.mediaplayer.intent.CHOSEN_SONG_INDEX
import com.example.mediaplayer.intent.NotificationAction.NOTIFICATION
import com.example.mediaplayer.repositry.TrackRepositoryFactory.provideTrackRepository
import com.example.mediaplayer.ui.chosenSong.adapter.CenterZoomLayoutManager
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import com.example.mediaplayer.viewModels.ChosenSongViewModelFactory
import com.example.mediaplayer.viewModels.createViewModel
import com.example.mediaplayer.viewModels.createViewModelFactory


/**
 * A simple [Fragment] subclass.
 */
class ChosenSongFragment : Fragment() {

    lateinit var viewModelFactory: ChosenSongViewModelFactory
    lateinit var viewModel: ChosenSongViewModel
    private lateinit var songListAdapter: SongListAdapter
    private lateinit var imageListAdapter: ImageListAdapter
    private lateinit var binding: ChosenSongFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        binding = ChosenSongFragmentBinding.inflate(inflater)
        val index: Int? = arguments?.getInt(CHOSEN_SONG_INDEX)
        val fromNotification = arguments?.getBoolean(NOTIFICATION, false)
        viewModelFactory = createViewModelFactory(requireActivity().application)!!
        viewModelFactory.apply {
            setData(index!!, fromNotification!!,
                    provideTrackRepository(requireActivity().application, provideDatabase(requireActivity().application)))
        }
        viewModel = createViewModel(this, viewModelFactory)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setUpSongRecyclerView()
        setUpImageRecyclerView()
        //requireActivity().disableActionBarTitle()
        if ((requireActivity() as AppCompatActivity).supportActionBar!!.isShowing)
            (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        setUpObserver()
        return binding.root
    }


    private fun setUpObserver() {
        with(viewModel)
        {
            chosenSongIndex.observe(viewLifecycleOwner, Observer { event ->
                event?.getContentIfNotHandled()?.let {
                    setCurrentPositionRecyclerView(it)
                    previousRecyclerViewPosition = it
                }
            })
            visualizerAnimationEnabled.observe(viewLifecycleOwner, Observer {
                it?.let {

                    binding.playerLayout.visualizerLayout.waveFormAnim.updateVisualizer(it)

                }
            })


        }
    }

    private fun setCurrentPositionRecyclerView(index: Int) {
        songListAdapter.setCurrentSelectedPosition(index)
        imageListAdapter.setCurrentSelectedPosition(index)
    }

    private fun setUpSongRecyclerView() {
        binding.playerLayout.listSong.layoutManager = CenterZoomLayoutManager(context!!)
        songListAdapter = SongListAdapter(viewModel, binding.playerLayout.listSong, binding.playerLayout.listSong.layoutManager as CenterZoomLayoutManager)
        binding.playerLayout.listSong.adapter = songListAdapter


    }

    private fun setUpImageRecyclerView() {
        binding.playerLayout.listImage.layoutManager = CenterZoomLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        imageListAdapter = ImageListAdapter(viewModel, binding.playerLayout.listImage, binding.playerLayout.listImage.layoutManager as CenterZoomLayoutManager)
        binding.playerLayout.listImage.adapter = imageListAdapter

    }

}// Required empty public constructor
