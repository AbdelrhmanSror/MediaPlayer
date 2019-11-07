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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.ChosenSongListLayoutBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.startFavouriteAnimation
import com.example.mediaplayer.ui.OnItemClickListener
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




class SongListAdapter(private val listener: OnItemClickListener) : ListAdapter<SongModel, SongListAdapter.ViewHolder>(DiffCallBack) {

    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private var lastSelectedItemPosition: Int = 0
    private var currentSelectedItemPosition: Int = 0
    private var isSnapAttached = false
    private val snapHelper = LinearSnapHelper()
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

    class ViewHolder(val binding: ChosenSongListLayoutBinding, private val songListAdapter: SongListAdapter) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongModel, listener: OnItemClickListener) {
            binding.playlistModel = item
            binding.favouriteShape.setOnClickListener {

                listener.onFavouriteClick(adapterPosition)
                binding.favouriteShape.startFavouriteAnimation(item.isFavourite)
            }
            binding.songNumber.text = songListAdapter.context.getString(R.string.song_number, (adapterPosition + 1).toString())
            binding.root.setOnClickListener {
                songListAdapter.setCurrentSelectedPosition(adapterPosition)
                listener.onClick(adapterPosition)
            }
            binding.executePendingBindings()

        }

        /**
         * return the view that viewHolder will hold
         */
        companion object {
            fun from(parent: ViewGroup, songListAdapter: SongListAdapter): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ChosenSongListLayoutBinding.inflate(inflater)
                return ViewHolder(binding, songListAdapter)

            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent, this)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        updateCurrentSelectedView(holder.itemView, position)
        holder.bind(getItem(position), listener)


    }


    // update the current view and remove any state was existed before recycling
    private fun updateCurrentSelectedView(item: View, position: Int) {
        if (position == currentSelectedItemPosition) {
            item.divider.visibility = View.VISIBLE
            lastSelectedItemPosition = position
        } else {
            item.divider.visibility = View.GONE

        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        snapHelper.attachToRecyclerView(recyclerView)
        isSnapAttached = true
        context = recyclerView.context

    }

    fun setCurrentSelectedPosition(position: Int) {
        //update the current focused position
        if (currentSelectedItemPosition != position) {
            currentSelectedItemPosition = position
            notifyItemChanged(lastSelectedItemPosition)
            notifyItemChanged(currentSelectedItemPosition)
            scrollTo(position)
        }


    }


    private fun scrollTo(position: Int) {
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
}


