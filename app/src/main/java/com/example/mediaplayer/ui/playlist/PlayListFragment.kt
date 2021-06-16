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

package com.example.mediaplayer.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.mediaplayer.R
import com.example.mediaplayer.database.provideDatabase
import com.example.mediaplayer.databinding.PlaylistFragmentBinding
import com.example.mediaplayer.intent.CHOSEN_SONG_INDEX
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.model.toSongModel
import com.example.mediaplayer.repositry.provideTrackRepository
import com.example.mediaplayer.ui.ClickType
import com.example.mediaplayer.ui.OnItemClickListener
import com.example.mediaplayer.viewModels.PlayListViewModel
import com.example.mediaplayer.viewModels.PlayListViewModelFactory
import com.example.mediaplayer.viewModels.createViewModel
import com.example.mediaplayer.viewModels.createViewModelFactory


/**
 * A simple [Fragment] subclass.
 */
class PlayListFragment : Fragment() {

    lateinit var viewModelFactory: PlayListViewModelFactory

    lateinit var viewModel: PlayListViewModel

    private lateinit var navController: NavController
    private lateinit var binding: PlaylistFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = PlaylistFragmentBinding.inflate(inflater)
        viewModelFactory = createViewModelFactory(requireActivity().application)!!
        viewModelFactory.setData(provideTrackRepository(requireActivity().application, provideDatabase(requireActivity().application)))
        viewModel = createViewModel(this, viewModelFactory)
        //find the nav controller so i can use it to navigate
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        prepareMusicList()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }


    private fun prepareMusicList() {
        setUpPlayList()
        //observing any changes to the playlist
        viewModel.playLists.observe(viewLifecycleOwner, androidx.lifecycle.Observer { playlist ->
            if (playlist.isNullOrEmpty()) {
                showPlaylistEmptyText()
                // binding.bottomSheetLayout.visibility = View.GONE
            } else {
                hidePlaylistEmptyText()

            }
            submitPlaylistToAdapter(playlist.toSongModel())

        })

    }

    private fun submitPlaylistToAdapter(songModel: List<SongModel>) {
        (binding.listSong.adapter as PlaylistAdapter).submitList(songModel)

    }

    private fun hidePlaylistEmptyText() {
        binding.noAudioText.visibility = View.GONE
    }

    private fun showPlaylistEmptyText() {
        binding.noAudioText.visibility = View.VISIBLE
    }

    private fun setUpPlayList() {
        //setup recycler view with adapter
        binding.listSong.apply {
            this.adapter = createAdapter()
        }

    }

    private fun createAdapter(): PlaylistAdapter {
        //creating adapter and set it with the playlist
        return PlaylistAdapter(object : OnItemClickListener {
            override fun onClick(clickType: ClickType, itemClickIndex: Int) {
                this@PlayListFragment.onClick(clickType, itemClickIndex)
            }
        })
    }

    private fun onClick(clickType: ClickType, itemClickIndex: Int) {
        when (clickType) {
            ClickType.RUN -> navigateAndPlayAudio(itemClickIndex)
            ClickType.FAVOURITE -> addToFavourite(viewModel.playLists.value!![itemClickIndex].toSongModel())
            ClickType.EDIT -> {
            }
            ClickType.DELETE -> {
            }
        }
    }

    private fun addToFavourite(songModel: SongModel) {
        viewModel.addToFavourite(songModel)
    }

    private fun navigateAndPlayAudio(position: Int) {
        val args = Bundle()
        args.putInt(CHOSEN_SONG_INDEX, position)
        navController.navigate(R.id.action_playListFragment_to_chosenSong_dest, args)
    }


}// Required empty public constructor



