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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.ChosenSongListLayoutBinding
import com.example.mediaplayer.extensions.startFavouriteAnimation
import com.example.mediaplayer.model.SongModel
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

class SongListAdapter(private val viewModel: ChosenSongViewModel, private val recyclerView: RecyclerView, layoutManager: LinearLayoutManager) :
        MediaAdapter<SongListAdapter.ViewHolder, SongModel>(recyclerView, layoutManager, DiffCallBack) {

    private var lastSelectedItemPosition: Int = 0
    private var currentSelectedItemPosition: Int = 0
    private var equalizerEnabled: Boolean = true
    private var visualizerEnabled: Boolean = true


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

    init {
        currentSelectedItemPosition = viewModel.previousRecyclerViewPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        updateCurrentSelectedView(holder.itemView, position)
        holder.bind(getItem(position), viewModel)


    }


    override fun setCurrentSelectedPosition(position: Int) {
        //update the current focused position
        if (currentSelectedItemPosition != position) {
            currentSelectedItemPosition = position
            notifyItemChanged(lastSelectedItemPosition)
            notifyItemChanged(currentSelectedItemPosition)
            speed = 5f
            scrollToPosition(position, true)
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
            if (byteArray[0].compareTo(-128) == 0) this.stopBars() else this.animateBars(byteArray)
        }

    }

    fun visualizerEnabled(byteArray: ByteArray) {
        visualizerEnabled = true
        recyclerView.findViewHolderForAdapterPosition(currentSelectedItemPosition)?.itemView?.wave_form_anim?.apply {
            this.updateVisualizer(byteArray)
        }

    }


}


