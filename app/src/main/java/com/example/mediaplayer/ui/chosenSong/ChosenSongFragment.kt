package com.example.mediaplayer.ui.chosenSong

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.example.mediaplayer.databinding.ChosenSongFragmentBinding
import com.example.mediaplayer.shared.CHOSEN_SONG_INDEX
import com.example.mediaplayer.shared.PlayerDestinations
import com.example.mediaplayer.ui.chosenSong.adapter.CenterZoomLayoutManager
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.ScrollingBehaviour
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class ChosenSongFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<ChosenSongViewModel> { viewModelFactory }
    private lateinit var songListAdapter: SongListAdapter
    private lateinit var imageListAdapter: ImageListAdapter
    private lateinit var binding: ChosenSongFragmentBinding
    var index: Int? = 0
    val fromNotification = arguments?.getBoolean(PlayerDestinations.NOTIFICATION, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        binding = ChosenSongFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setUpSongRecyclerView()
        setUpImageRecyclerView()
        setCurrentPositionRecyclerView(arguments?.getInt(CHOSEN_SONG_INDEX, 0)!!, true)
        setUpObserver()
        return binding.root
    }


    private fun setUpObserver() {
        with(viewModel)
        {

            //restore the previous recycler view position after configuration changes
            if (viewModel.previousRecyclerViewPosition != -1) {
                setCurrentPositionRecyclerView(viewModel.previousRecyclerViewPosition, false)
            }
            chosenSongIndex.observe(viewLifecycleOwner, Observer { event ->
                event?.getContentIfNotHandled()?.let {
                    setCurrentPositionRecyclerView(it, true)
                    previousRecyclerViewPosition = it
                }
            })
            visualizerAnimationEnabled.observe(viewLifecycleOwner, Observer {
                it?.let {
                    songListAdapter.visualizerEnabled(it)

                }
            })


        }
    }

    private fun setCurrentPositionRecyclerView(index: Int, scrollEnabled: Boolean) {
        songListAdapter.setCurrentSelectedPosition(index, scrollEnabled)
        imageListAdapter.setCurrentSelectedPosition(index, scrollEnabled)
    }

    private fun setUpSongRecyclerView() {
        songListAdapter = SongListAdapter(viewModel, object : ScrollingBehaviour {
            override fun onScrollEnd(index: Int) {
                Log.v("scrollingbehaviour", "done")
                viewModel.seekTo(index)
            }
        })
        binding.playerLayout.listSong.layoutManager = CenterZoomLayoutManager(context!!)
        binding.playerLayout.listSong.adapter = songListAdapter

    }

    private fun setUpImageRecyclerView() {
        imageListAdapter = ImageListAdapter(viewModel, null)
        binding.playerLayout.listImage.layoutManager = CenterZoomLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        binding.playerLayout.listImage.adapter = imageListAdapter

    }

}// Required empty public constructor
