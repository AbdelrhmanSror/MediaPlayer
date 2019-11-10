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
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.PlayerImageLibraryBinding
import com.example.mediaplayer.ui.OnItemClickListener


class ImageListAdapter(private val albumCoverUris: ArrayList<String?>, private val listener: OnItemClickListener) : RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private var currentSelectedItemPosition: Int = -1
    private var isSnapAttached = false
    private val snapHelper = LinearSnapHelper()
    private val smoothScroller: LinearSmoothScroller by lazy {
        object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }
        }
    }

    class ViewHolder(val binding: PlayerImageLibraryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String?, listener: OnItemClickListener) {
            binding.imageUri = item
            binding.root.setOnClickListener {
                listener.onClick(adapterPosition)
            }
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
        holder.bind(albumCoverUris[position], listener)


    }

    override fun getItemCount(): Int {
        return albumCoverUris.size
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        snapHelper.attachToRecyclerView(recyclerView)
        this.context = recyclerView.context
        isSnapAttached = true

    }

    fun setCurrentSelectedPosition(position: Int) {
        if (currentSelectedItemPosition != position) {
            currentSelectedItemPosition = position
            //scroll to the current focused position
            scrollTo(position)
        }


    }

    private fun scrollTo(position: Int) {
        Log.v("heyfroObserver", "positon $position")

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
        smoothScroller.targetPosition = position
        (recyclerView.layoutManager as CenterZoomLayoutManager).startSmoothScroll(smoothScroller)
    }

}