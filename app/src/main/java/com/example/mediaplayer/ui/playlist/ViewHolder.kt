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
 *//*


package com.example.mediaplayer.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.BR
import com.example.mediaplayer.R
import com.example.mediaplayer.databinding.PlaylistLayoutBinding
import com.example.mediaplayer.databinding.PlaylistLayoutBottomSheetBinding
import com.example.mediaplayer.model.SongModel
import kotlinx.android.synthetic.main.playlist_layout.view.*
import kotlinx.android.synthetic.main.playlist_layout_bottom_sheet.view.*

enum class ViewHolderType {
    PLAYLIST_VIEW_HOLDER
    ,
    PLAYLIST_VIEW_HOLDER_BOTTOM_SHEET
}

*/
/**
 *
 * for making viewHolder all u need just implement ViewHolderInterface
 *//*

class ViewHolder private constructor(rootView: View, private val viewHolder: ViewHolderInterface) : RecyclerView.ViewHolder(rootView){


    fun bind(item: SongModel ,listener: OnItemClickListener) {
        viewHolder.bind(item, listener)
    }

    companion object {
        fun from(parent: ViewGroup, viewHolderType: ViewHolderType): ViewHolder {
            val viewHolder = ViewHolderFactory.create(viewHolderType)
            return ViewHolder(viewHolder.from(parent), viewHolder)
        }

    }
}


object ViewHolderFactory {
    fun create(viewHolderType: ViewHolderType): ViewHolderInterface {
        return when (viewHolderType) {
            ViewHolderType.PLAYLIST_VIEW_HOLDER -> PlayListViewHolder()
            ViewHolderType.PLAYLIST_VIEW_HOLDER_BOTTOM_SHEET -> BottomSheetViewHolder()
        }
    }
}


interface ViewHolderInterface {

    */
/**
     * method for binding the view with its data
 *//*

    fun bind(itemClickedPosition: SongModel, listener: OnItemClickListener)

    */
/**
     * method responsible for inflating the view using data binding
     * @return ViewDataBinding object
 *//*


    fun from(parent: ViewGroup): View

}

private class PlayListViewHolder : ViewHolderInterface {
    private lateinit var binding: ViewDataBinding
    override fun bind(item: SongModel,listener: OnItemClickListener) {
        if (::binding.isInitialized) {
            binding.setVariable(BR.playlistModel, item)
            binding.root.playlistContainer.setOnClickListener { listener.onClick(itemClickedPosition) }
        }
    }


    */
/**
     * return the view that viewHolder will hold
 *//*

    override fun from(parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate<PlaylistLayoutBinding>(inflater, R.layout.playlist_layout, parent, false)
        return binding.root
    }


}

private class BottomSheetViewHolder : ViewHolderInterface {
    private lateinit var binding: ViewDataBinding
    override fun bind(itemClickedPosition: Int, songs: List<SongModel>, listener: OnItemClickListener) {
        if (::binding.isInitialized) {
            binding.setVariable(BR.playlistModelB, songs[itemClickedPosition])
            //set for every song a number sequentially in bottom sheet
            val songNumString: String = when (itemClickedPosition in 0..8) {
                true -> "0${1 + itemClickedPosition}"
                else -> "${1 + itemClickedPosition}"
            }
            binding.root.song_number_bottom_sheet.text = songNumString
            binding.root.bottom_sheet_playlist_container.setOnClickListener { listener.onClick(songs, itemClickedPosition) }
        }
    }

    override fun from(parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate<PlaylistLayoutBottomSheetBinding>(inflater, R.layout.playlist_layout_bottom_sheet, parent, false)
        return binding.root
    }


}
*/
