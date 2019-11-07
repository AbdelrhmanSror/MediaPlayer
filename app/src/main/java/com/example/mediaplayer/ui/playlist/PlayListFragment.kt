package com.example.mediaplayer.ui.playlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.mediaplayer.*
import com.example.mediaplayer.database.toSongModel
import com.example.mediaplayer.databinding.PlaylistFragmentBinding
import com.example.mediaplayer.foregroundService.AudioForegroundService
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.ui.OnItemClickListener
import com.example.mediaplayer.viewModels.PlayListViewModel
import com.example.mediaplayer.viewModels.PlayListViewModelFactory
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class PlayListFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var binding: PlaylistFragmentBinding
    private lateinit var playList: List<SongModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = PlaylistFragmentBinding.inflate(inflater)
        //find the nav controller so i can use it to navigate
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        prepareMusicList()
        return binding.root
    }


    private fun prepareMusicList() {
        val factory = PlayListViewModelFactory(activity!!.application)
        val playListViewModel = ViewModelProviders.of(activity!!, factory).get(PlayListViewModel::class.java)
        setUpPlayList()
        playListViewModel.playLists.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if (it.isNullOrEmpty()) {
                binding.noAudioText.visibility = View.VISIBLE
                // binding.bottomSheetLayout.visibility = View.GONE
            } else {
                binding.noAudioText.visibility = View.GONE
                playList = it.toSongModel()
                (binding.listSong.adapter as PlaylistAdapter).submitList(playList)

            }

        })

    }

    private fun setUpPlayList() {
        //creating adapter and set it with the playlist
        val adapter = PlaylistAdapter(object : OnItemClickListener {
            override fun onClick(itemClickIndex: Int) {
                startForeground(playList as ArrayList, itemClickIndex)
                navController.navigate(R.id.action_playListFragment_to_chosenSongFragment)
            }
        })
        //setup recycler view with adapter
        binding.listSong.adapter = adapter

    }

    /* private fun setUpPlayListBottomSheet(songList: List<SongModel>) {
         var itemClickedPosition = 0
         val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.bottomSheet)
         binding.bottomSheetLayout.bottomSheet.setOnClickListener {
             bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
         }

         //adapter for bottom sheet list
         val adapterBottomSheet = PlaylistAdapter(songList, ViewHolderType.PLAYLIST_VIEW_HOLDER_BOTTOM_SHEET, OnItemClickListener { playLists, itemClickIndex ->
             startForeground(playLists as ArrayList<SongModel>, itemClickIndex)
             val view = binding.bottomSheetLayout.playlist_bottom_sheet.findViewHolderForAdapterPosition(itemClickIndex)
             view?.itemView?.apply {
                 itemClickedPosition = itemClickIndex
                 equalizer_replacement.visibility = View.GONE
                 binding.bottomSheetLayout.playlist_bottom_sheet.setRecyclerListener {

                     if (it.itemView == view) {
                         Log.v("recleradapter", "${it.adapterPosition}")
                         it.itemView.equalizer_replacement.visibility = View.GONE
                     } else
                         it.itemView.equalizer_replacement.visibility = View.VISIBLE

                 }
             }
         })
         //setup recyclerview with adapter
         binding.bottomSheetLayout.playlist_bottom_sheet.adapter = adapterBottomSheet
     }*/

    private fun startForeground(song: ArrayList<SongModel>, chosenSongIndex: Int) {
        val foregroundIntent = Intent(activity, AudioForegroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND.value
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, song)
        activity?.startForeground(foregroundIntent)
    }

}// Required empty public constructor



