/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mediaplayer.ui.favourite


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.mediaplayer.*
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.databinding.FragmentFavouriteBinding
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.ui.OnItemClickListener
import com.example.mediaplayer.viewModels.FavouriteSongViewModel
import com.example.mediaplayer.viewModels.FavouriteSongViewModelFactory
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class FavouriteFragment : Fragment() {
    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var navController: NavController
    private lateinit var playList: List<SongModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavouriteBinding.inflate(inflater)
        //find the nav controller so i can use it to navigate
        navController = Navigation.findNavController(Objects.requireNonNull<FragmentActivity>(activity), R.id.nav_host_fragment)

        prepareMusicList()
        return binding.root
    }

    private fun prepareMusicList() {
        val factory = FavouriteSongViewModelFactory(activity!!.application)
        val favouriteSongViewModel = ViewModelProviders.of(activity!!, factory).get(FavouriteSongViewModel::class.java)
        setUpPlayList()

        favouriteSongViewModel.playLists.observe(viewLifecycleOwner, Observer {
            Log.v("favouriteSize", "${it.size}")
            it?.let {
                playList = it.toSongModel()
                (binding.listSong.adapter as FavouriteSongAdapter).submitList(playList)

            }


        })

    }

    private fun setUpPlayList() {
        //creating adapter and set it with the playlists
        val adapter = FavouriteSongAdapter(object : OnItemClickListener {
            override fun onClick(itemClickIndex: Int) {
                startForeground(playList, itemClickIndex)

            }
        })
        //setup recyclerview with adapter
        binding.listSong.adapter = adapter
    }

    private fun startForeground(songModels: List<SongModel>, itemClickedIndex: Int) {
        val foregroundIntent = Intent(activity, AudioForegroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND.value
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, itemClickedIndex)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, songModels as ArrayList)
        activity?.startForeground(foregroundIntent)

    }

}

