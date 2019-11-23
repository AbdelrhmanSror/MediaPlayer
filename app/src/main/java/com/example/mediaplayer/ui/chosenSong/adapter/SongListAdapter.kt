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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.mediaplayer.databinding.ChosenSongListLayoutBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.startFavouriteAnimation
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        updateCurrentSelectedView(holder.itemView, position)
        holder.bind(getItem(position), viewmodel)


    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        Log.v("attachectowinfndjbfd", "${holder.adapterPosition}")
        super.onViewAttachedToWindow(holder)
    }
/*private fun onfinishIntflatingFirstTime(onFinish: () -> Unit) {
        if (firstTimeInflating) {
            recyclerView.viewTreeObserver
                    .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            onFinish()
                            Log.v("inflatingTime","fisttime")
                            firstTimeInflating=false
                            //At this point the layout is complete and the
                            //dimensions of recyclerView and any child views are known.
                            //Remove listener after changed RecyclerView's height to prevent infinite loop
                            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })
        } else {
            Log.v("inflatingTime","secondtime")
            onFinish()
        }
    }
*/

    // update the current view and remove any state was existed before recycling
    private fun updateCurrentSelectedView(item: View, position: Int) {
        if (position == currentSelectedItemPosition) {
            item.divider.visibility = View.VISIBLE
            // item.equalizer_anim.visibility = View.VISIBLE
            lastSelectedItemPosition = position
            /*  val animatedVector = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_anim)
              item.equalizer_anim.setImageDrawable(animatedVector)
              val mainHandler = Handler(Looper.getMainLooper())
              animatedVector?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                  override fun onAnimationEnd(drawable: Drawable?) {
                      mainHandler.post {
                          Log.v("animationStart", "yeah")
                          animatedVector.start()

                      }
                  }
              })
              animatedVector?.start()*/
        } else {
            item.divider.visibility = View.GONE
            // item.equalizer_anim.visibility = View.GONE


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


