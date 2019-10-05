package com.example.mediaplayer.ui.playlist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.mediaplayer.*
import com.example.mediaplayer.databinding.PlaylistFragmentBinding
import com.example.mediaplayer.foregroundService.AudioForgregroundService
import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.viewModels.PlayListViewModel
import com.example.mediaplayer.viewModels.PlayListViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.playlist_layout_bottom_sheet.view.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class PlayListFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var binding: PlaylistFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = PlaylistFragmentBinding.inflate(inflater)
        //find the nav controller so i can use it to navigate
        navController = Navigation.findNavController(Objects.requireNonNull<FragmentActivity>(activity), R.id.nav_host_fragment)



        return binding.root
    }

    private fun navigate(playListModels: List<PlayListModel>, itemClickedIndex: Int) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(LIST_SONG, playListModels as ArrayList)
        bundle.putInt(CHOSEN_SONG_INDEX, itemClickedIndex)
        bundle.putString(FRAGMENT_PURPOSE, PlayerActions.ACTION_FOREGROUND.value)
        navController.navigate(R.id.action_playListFragment_to_chosenSongFragment, bundle)
    }

    override fun onStart() {
        super.onStart()
        checkPermission()

    }

    private fun prepareMusicList() {
        val factory = PlayListViewModelFactory(activity!!.application)
        val playListViewModel = ViewModelProviders.of(activity!!, factory).get(PlayListViewModel::class.java)
        playListViewModel.playLists.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it.isNullOrEmpty()) {
                binding.noAudioText.visibility = View.VISIBLE
                binding.bottomSheetLayout.visibility = View.GONE
            } else {
                //display the main list of media
                setUpPlayList(it)

                //display the list of bottom sheet and setup bottom sheet behaviour
                setUpPlayListBottomSheet(it)
            }

        })

    }

    private fun setUpPlayList(playListModels: List<PlayListModel>) {
        //creating adapter and set it with the playlists
        val adapter = PlaylistAdapter(playListModels, ViewHolderType.PLAYLIST_VIEW_HOLDER, OnClickListener { playLists, itemClickIndex ->
            navigate(playLists, itemClickIndex)

        })
        //setup recyclerview with adapter
        binding.playList.adapter = adapter
    }

    private fun setUpPlayListBottomSheet(playListModels: List<PlayListModel>) {
        var itemClickedPosition = 0
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayout.bottomSheet)
        binding.bottomSheetLayout.bottomSheet.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        //adapter for bottom sheet list
        val adapterBottomSheet = PlaylistAdapter(playListModels, ViewHolderType.PLAYLIST_VIEW_HOLDER_BOTTOM_SHEET, OnClickListener { playLists, itemClickIndex ->
            startForeground(playLists as ArrayList<PlayListModel>, itemClickIndex)
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
    }

    private fun startForeground(playList: ArrayList<PlayListModel>, chosenSongIndex: Int) {
        val foregroundIntent = Intent(activity, AudioForgregroundService::class.java)
        foregroundIntent.action = PlayerActions.ACTION_FOREGROUND.value
        foregroundIntent.putExtra(CHOSEN_SONG_INDEX, chosenSongIndex)
        foregroundIntent.putParcelableArrayListExtra(LIST_SONG, playList)
        activity?.startForeground(foregroundIntent)
    }

    private fun checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull<FragmentActivity>(activity),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(activity, "You should grant this permission so application can access your audio files", Toast.LENGTH_LONG).show()
                // No explanation needed; request the permission
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Permission.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE.value)

            } else {
                // No explanation needed; request the permission
                requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Permission.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE.value)

            }
        } else {

            // Permission has already been granted
            prepareMusicList()

        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permission.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE.value) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has already been granted
                prepareMusicList()
            }
        }

    }

    enum class Permission(val value: Int) {
        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE(5)
    }


}// Required empty public constructor
