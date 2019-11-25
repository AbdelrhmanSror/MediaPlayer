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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.PlaylistLayoutBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.ui.OnItemClickListener


class FavouriteSongAdapter(private val listener: OnItemClickListener) : ListAdapter<SongModel, FavouriteSongAdapter.ViewHolder>(DiffCallBack) {

    /**
     * diff util class to calculate the difference between two list if the the old list has changed
     *with minimum changes it can do
     */
    object DiffCallBack : DiffUtil.ItemCallback<SongModel>() {
        override fun areItemsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem.title == newItem.title
        }

    }

    class ViewHolder(val binding: PlaylistLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongModel, listener: OnItemClickListener) {
            binding.playlistModel = item
            binding.playlistContainer.setOnClickListener { listener.onClick(adapterPosition) }

        }


        /**
         * return the view that viewHolder will hold
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PlaylistLayoutBinding.inflate(inflater)
                return ViewHolder(binding)

            }
        }


    }

    private fun setOnItemClickListener(){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, listener)


    }


}
