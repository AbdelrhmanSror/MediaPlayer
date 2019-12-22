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
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.PlayerImageLibraryBinding
import com.example.mediaplayer.viewModels.ChosenSongViewModel
import kotlinx.android.synthetic.main.player_image_library.view.*


class ImageListAdapter(private val viewModel: ChosenSongViewModel, recyclerView: RecyclerView, layoutManager: LinearLayoutManager) :
        MediaAdapter<ImageListAdapter.ViewHolder, String>(recyclerView, layoutManager, DiffCallBack) {

    private var currentSelectedItemPosition: Int = -1


    /**
     *diff util class to calculate the difference between two list if the the old list has changed
     *with minimum changes it can do
     */
    object DiffCallBack : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }

    }


    class ViewHolder(val binding: PlayerImageLibraryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: ChosenSongViewModel) {
            binding.viewmodel = viewModel
            binding.itemPosition = adapterPosition
            binding.executePendingBindings()

        }


        /**
         * return the viewHolder
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PlayerImageLibraryBinding.inflate(inflater)
                return ViewHolder(binding)

            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ViewCompat.setTransitionName(holder.itemView.artwork, "Test_$position")
        holder.bind(viewModel)


    }


    override fun setCurrentSelectedPosition(position: Int, scrollEnabled: Boolean) {
        if (currentSelectedItemPosition != position) {
            currentSelectedItemPosition = position
            //scroll to the current focused position
            if (scrollEnabled) {
                speed = 1f
                scrollToPosition(position)
            }
        }


    }


}