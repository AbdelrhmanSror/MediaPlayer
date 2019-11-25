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
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.mediaplayer.databinding.PlayerImageLibraryBinding
import com.example.mediaplayer.viewModels.ChosenSongViewModel


class ImageListAdapter(private val viewmodel: ChosenSongViewModel) : ListAdapter<String, ImageListAdapter.ViewHolder>(DiffCallBack) {

    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private var currentSelectedItemPosition: Int = -1
    private var isSnapAttached = false
    private var firstTimeInflating: Boolean = true
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
    object DiffCallBack : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }

    }


    class ViewHolder(val binding: PlayerImageLibraryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viewmodel: ChosenSongViewModel) {
            binding.viewmodel = viewmodel
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
        holder.bind(viewmodel)


    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        snapHelper.attachToRecyclerView(recyclerView)
        this.context = recyclerView.context
        isSnapAttached = true

    }

    fun setCurrentSelectedPosition(position: Int, scrollEnabled: Boolean) {
        if (currentSelectedItemPosition != position) {
            currentSelectedItemPosition = position
            //scroll to the current focused position
            if (scrollEnabled)
                scrollTo(position)
        }


    }


    private fun firstTimeInstantScrolling(position: Int) {
        snapHelper.attachToRecyclerView(recyclerView)
        Handler().postDelayed({
            //instant scroll at first time recyclerview started
            //if position is 0 then instant scroll is enought
            //otherwise we make instant scroll and then we convert it to smooth scroll by adding 1
            if (position == 0)
                recyclerView.scrollToPosition(position)
            else {
                recyclerView.scrollToPosition(position)
                smoothScroller.targetPosition = 1
                (recyclerView.layoutManager as CenterZoomLayoutManager).startSmoothScroll(smoothScroller)
            }
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
        smoothScroller.targetPosition = position
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