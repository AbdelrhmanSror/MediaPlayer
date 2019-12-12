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

package com.example.mediaplayer.ui.chosenSong.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.mediaplayer.databinding.ChosenSongListLayoutBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.shared.startFavouriteAnimation
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import kotlinx.android.synthetic.main.chosen_song_list_layout.view.*


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




class SongListAdapter(private val viewmodel: ChosenSongViewModel) : ListAdapter<SongModel, SongListAdapter.ViewHolder>(DiffCallBack) {

    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private var lastSelectedItemPosition: Int = 0
    private var currentSelectedItemPosition: Int = 0
    private var firstTimeInflating: Boolean = true
    private var isSnapAttached = false
    private val snapHelper = LinearSnapHelper()
    private var equalizerEnabled: Boolean = true
    private var visualizerEnabled: Boolean = true

    private val smoothScroller: LinearSmoothScroller by lazy {
        object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }
    }

    /**
     *diff util class to calculate the difference between two list if the the old list has changed
     *with minimum changes it can do
     */
    object DiffCallBack : DiffUtil.ItemCallback<SongModel>() {

        override fun areItemsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem.isFavourite == newItem.isFavourite
        }

    }

    class ViewHolder(val binding: ChosenSongListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongModel, viewModel: ChosenSongViewModel) {
            binding.playlistModel = item
            binding.viewmodel = viewModel
            binding.itemPosition = adapterPosition
            binding.favouriteShape.setOnClickListener {
                binding.favouriteShape.startFavouriteAnimation(item.isFavourite)
            }
            /*viewModel.playPauseState.observeForever(Observer {
                it?.getContentIfNotHandled()?.let {
                    if (it) binding.equalizerAnim.animateBars() else binding.equalizerAnim.stopBars()

                }
            })*/
            binding.executePendingBindings()

        }

        /**
         * return the view that viewHolder will hold
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ChosenSongListLayoutBinding.inflate(inflater)
                return ViewHolder(binding)

            }

        }


    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        snapHelper.attachToRecyclerView(recyclerView)
        isSnapAttached = true
        context = recyclerView.context
        onRecyclerViewScrolling()

    }

    private fun onRecyclerViewScrolling() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        updateCurrentSelectedView(holder.itemView, position)
        holder.bind(getItem(position), viewmodel)


    }


    fun setCurrentSelectedPosition(position: Int, scrollEnabled: Boolean) {
        //update the current focused position
        if (currentSelectedItemPosition != position) {
            currentSelectedItemPosition = position
            if (!scrollEnabled)
                return
            notifyItemChanged(lastSelectedItemPosition)
            notifyItemChanged(currentSelectedItemPosition)
            scrollTo(position)

        }


    }

    // update the current view and remove any state was existed before recycling
    private fun updateCurrentSelectedView(item: View, position: Int) {
        // Log.v("playpausestate", "$equalizerEnabled  $position  rec upcu")

        if (position == currentSelectedItemPosition) {
            item.divider.visibility = View.VISIBLE
            item.wave_form_anim.visibility = View.VISIBLE
             //item.equalizer_anim.visibility = View.VISIBLE
            lastSelectedItemPosition = position


        } else {
            item.divider.visibility = View.GONE
            item.wave_form_anim.visibility = View.GONE
            //item.equalizer_anim.visibility = View.GONE
            item.equalizer_anim.stopBars()


        }
    }


    fun equalizerEnabled(byteArray: ByteArray) {
        equalizerEnabled = true
        recyclerView.findViewHolderForAdapterPosition(currentSelectedItemPosition)?.itemView?.equalizer_anim?.apply {
            if (byteArray[0].compareTo(-128)==0) this.stopBars() else this.animateBars(byteArray)
        }

    }
    fun visualizerEnabled(byteArray: ByteArray) {
        visualizerEnabled = true
        recyclerView.findViewHolderForAdapterPosition(currentSelectedItemPosition)?.itemView?.wave_form_anim?.apply {
            this.updateVisualizer(byteArray)
        }

    }


    private fun firstTimeInstantScrolling(position: Int) {
        snapHelper.attachToRecyclerView(recyclerView)
        Handler().postDelayed({
            //instant scroll at first time recyclerview started
            recyclerView.scrollToPosition(position + 1)

        }, 200)
        firstTimeInflating = false
    }

    private fun normalScrolling(position: Int) {
        //if position was the first or last then just scroll
        if (position == 0 || position == itemCount - 1) {
            if (isSnapAttached) {
                snapHelper.attachToRecyclerView(null)
                isSnapAttached = false
            }

        } else {
            //we attach snaphelper if it is not
            if (!isSnapAttached) {
                snapHelper.attachToRecyclerView(recyclerView)
                isSnapAttached = true
            }
        }
        smoothScroller.targetPosition = position + 1
        (recyclerView.layoutManager as CenterZoomLayoutManager).startSmoothScroll(smoothScroller)
    }

    private fun scrollTo(position: Int) {

        if (firstTimeInflating) {
            firstTimeInstantScrolling(position)
        } else {
            normalScrolling(position)
        }
    }

}


