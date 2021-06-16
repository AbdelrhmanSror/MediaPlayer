/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.ui.playlist


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.PlaylistLayoutBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.ui.ClickType
import com.example.mediaplayer.ui.OnItemClickListener
import kotlinx.android.synthetic.main.playlist_layout.view.*


class PlaylistAdapter(private val itemListener: OnItemClickListener) : ListAdapter<SongModel, PlaylistAdapter.ViewHolder>(DiffCallBack) {

    /**
     *diff util class to calculate the difference between two list if the the old list has changed
     *with minimum changes it can do
     */
    object DiffCallBack : DiffUtil.ItemCallback<SongModel>() {
        override fun areItemsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem.id == newItem.id
        }

    }

    class ViewHolder(val binding: PlaylistLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongModel, listener: OnItemClickListener) {
            binding.playlistModel = item
            binding.itemPosition = adapterPosition

            binding.spinner.setOnClickListener {
                //creating a popup menu
                val popup = PopupMenu(binding.spinner.context, binding.spinner)
                //inflating menu from xml resource
                popup.inflate(R.menu.track_option_menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.edit -> {
                            onEdit()
                        }
                        R.id.delete -> {
                            onDelete()
                        }
                        R.id.add_to_favourite -> {
                            addToFav(listener, adapterPosition)
                        }
                        else -> false
                    }

                }

                //displaying the popup
                popup.show()

            }
            binding.root.playlistContainer.setOnClickListener { listener.onClick(ClickType.RUN, adapterPosition) }
        }

        private fun onEdit(): Boolean {
            Toast.makeText(binding.spinner.context, "edit", Toast.LENGTH_LONG).show()
            return true

        }

        private fun onDelete(): Boolean {
            Toast.makeText(binding.spinner.context, "delete", Toast.LENGTH_LONG).show()
            return true
        }

        private fun addToFav(listener: OnItemClickListener, position: Int): Boolean {
            listener.onClick(ClickType.FAVOURITE, position)
            Toast.makeText(binding.spinner.context, "add to favourite", Toast.LENGTH_LONG).show()

            return true
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        ViewCompat.setTransitionName(holder.itemView.AlbumArt, "Test_$position")

        holder.bind(getItem(position)!!, itemListener)
    }


}
