package com.example.mediaplayer.ui.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.mediaplayer.R
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.databinding.PlaylistFragmentBinding
import com.example.mediaplayer.shared.CHOSEN_SONG_INDEX
import com.example.mediaplayer.ui.OnItemClickListener
import com.example.mediaplayer.viewModels.PlayListViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class PlayListFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<PlayListViewModel> { viewModelFactory }

    private lateinit var navController: NavController
    private lateinit var binding: PlaylistFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = PlaylistFragmentBinding.inflate(inflater)
        //find the nav controller so i can use it to navigate
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        prepareMusicList()
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }


    private fun prepareMusicList() {
        setUpPlayList()
        viewModel.playLists.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if (it.isNullOrEmpty()) {
                binding.noAudioText.visibility = View.VISIBLE
                // binding.bottomSheetLayout.visibility = View.GONE
            } else {
                binding.noAudioText.visibility = View.GONE

            }
            (binding.listSong.adapter as PlaylistAdapter).submitList(it.toSongModel())

        })

    }

    override fun onStart() {
        Log.v("onstartfragment","start")
        super.onStart()
    }

    private fun setUpPlayList() {
        //creating adapter and set it with the playlist
        val adapter = PlaylistAdapter(object : OnItemClickListener {
            override fun onClick(itemClickIndex: Int) {
                val args = Bundle()
                args.putInt(CHOSEN_SONG_INDEX, itemClickIndex)
                navController.navigate(R.id.action_playListFragment_to_chosenSong_dest, args)
            }
        })
        //setup recycler view with adapter
        binding.listSong.apply {
            this.adapter=adapter
        }

    }



}// Required empty public constructor



