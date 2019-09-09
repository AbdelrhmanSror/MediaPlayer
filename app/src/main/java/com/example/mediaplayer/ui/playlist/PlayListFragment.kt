package com.example.mediaplayer.ui.playlist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.mediaplayer.CHOSEN_SONG_INDEX
import com.example.mediaplayer.LIST_SONG
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.PlaylistFragmentBinding
import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.viewModels.PlayListViewModel
import com.example.mediaplayer.viewModels.PlayListViewModelFactory
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
        navController.navigate(R.id.action_playListFragment_to_chosenSongFragment, bundle)
    }

    override fun onStart() {
        super.onStart()
        checkPermission()

    }

    private fun prepareMusicList() {
        val playListModels: List<PlayListModel>?
        val factory = PlayListViewModelFactory(activity!!.application)
        val playListViewModel = ViewModelProviders.of(activity!!, factory).get(PlayListViewModel::class.java)
        playListModels = playListViewModel.playLists
        if (playListModels.isNullOrEmpty()) {
            binding.noAudioText.visibility = View.VISIBLE
            return
        }
        //creating adapter and set it with the playlists
        val adapter = PlaylistAdapter(playListModels, object : PlaylistAdapter.OnClickListener {
            override fun onClick(playLists: List<PlayListModel>, itemClickIndex: Int) {
                navigate(playLists, itemClickIndex)

            }
        })
        //setup recyclerview with adapter
        binding.playList.adapter = adapter

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
