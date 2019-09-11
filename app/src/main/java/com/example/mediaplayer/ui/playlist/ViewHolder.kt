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

package com.example.mediaplayer.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.BR
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.PlaylistLayoutBinding
import com.example.mediaplayer.databinding.PlaylistLayoutBottomSheetBinding
import com.example.mediaplayer.model.PlayListModel
import kotlinx.android.synthetic.main.playlist_layout.view.*
import kotlinx.android.synthetic.main.playlist_layout_bottom_sheet.view.*

abstract class ViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

    /**
     * method for binding the view with its data
     */
    abstract fun bind(playLists: List<PlayListModel>, listener: OnClickListener)

}

interface PlayListViewHolderInterface {
    /**
     * method reponsible for inflating the view using databinding
     *
     * @param parent to get context from
     * @return ViewHolder object
     */

    fun from(parent: ViewGroup): ViewHolder

}

class PlayListViewHolder(private val binding: ViewDataBinding) : ViewHolder(binding) {
    override fun bind(playLists: List<PlayListModel>, listener: OnClickListener) {
        binding.setVariable(BR.playlistModel, playLists[adapterPosition])
        binding.root.playlistContainer.setOnClickListener { listener.onClick(playLists, adapterPosition) }
    }


    companion object : PlayListViewHolderInterface {
        override fun from(parent: ViewGroup): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<PlaylistLayoutBinding>(inflater, R.layout.playlist_layout, parent, false)
            return PlayListViewHolder(binding)
        }
    }


}

class PlayListBottomSheetViewHolder(private val binding: ViewDataBinding) : ViewHolder(binding) {
    override fun bind(playLists: List<PlayListModel>, listener: OnClickListener) {
        binding.setVariable(BR.playlistModelB, playLists[adapterPosition])
        //set for every song a number sequentially in bottom sheet
        val songNumString: String = when (adapterPosition in 0..8) {
            true -> "0${1 + adapterPosition}"
            else -> "${1 + adapterPosition}"
        }
        binding.root.song_number_bottom_sheet.text = songNumString
        binding.root.bottom_sheet_playlist_container.setOnClickListener { listener.onClick(playLists, adapterPosition) }
    }

    companion object : PlayListViewHolderInterface {
        override fun from(parent: ViewGroup): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<PlaylistLayoutBottomSheetBinding>(inflater, R.layout.playlist_layout_bottom_sheet, parent, false)
            return PlayListBottomSheetViewHolder(binding)
        }
    }

}
