package com.example.mediaplayer.ui.chosenSong.adapter

import android.util.DisplayMetrics
import androidx.recyclerview.widget.*
import com.example.mediaplayer.shared.CustomScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

abstract class MediaAdapter<VH : RecyclerView.ViewHolder, T>(diffUtil: DiffUtil.ItemCallback<T>) :
        ListAdapter<T, VH>(diffUtil), CoroutineScope by CustomScope(Dispatchers.Main) {
    protected lateinit var recyclerView: RecyclerView
    private var distance: Int = -1
    private var isSnapAttached = false
    private val snapHelper = LinearSnapHelper()
    private var isFirstTime = true
    private var selectedPosition = 0
    private var isListenerRegistered = false

    abstract fun setCurrentSelectedPosition(position: Int, scrollEnabled: Boolean)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        snapHelper.attachToRecyclerView(recyclerView)
        isSnapAttached = true


    }

    private suspend fun getDistance(): Int {
        return if (distance == -1) {
            delay(100)
            val firstPosition = (recyclerView.layoutManager as CenterZoomLayoutManager).findFirstVisibleItemPosition()
            val lastPosition = (recyclerView.layoutManager as CenterZoomLayoutManager).findLastVisibleItemPosition()
            distance = abs((lastPosition - firstPosition) / 2)
            distance
        } else {
            distance
        }
    }


    companion object {
        private const val MILLISECONDS_PER_INCH = 8f //default is 25f (bigger = slower)
    }

    private val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val firstPosition = (recyclerView.layoutManager as CenterZoomLayoutManager).findFirstCompletelyVisibleItemPosition()
                val lastPosition = (recyclerView.layoutManager as CenterZoomLayoutManager).findLastCompletelyVisibleItemPosition()
                /**
                 * if the scroller did not scroll to the selected position we scroll again until we get it right
                 */
                if (selectedPosition.coerceIn(firstPosition, lastPosition) != selectedPosition) {
                    scrollTo(selectedPosition)
                } else {
                    if (isFirstTime) {
                        isFirstTime = false
                    }
                    launch {
                        delay(100)
                    }
                    isListenerRegistered = false
                    recyclerView.removeOnScrollListener(this)
                }
            }
        }
    }
    private val smoothScroller: LinearSmoothScroller by lazy {
        object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_END
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }
    }


    private fun normalScrolling(position: Int) {
        //if position was the first or last then just scroll
        if (position == 0 || position == itemCount) {
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
        startSmoothScrolling(position)
    }

    private fun startSmoothScrolling(position: Int) {
        smoothScroller.targetPosition = position
        (recyclerView.layoutManager as CenterZoomLayoutManager).startSmoothScroll(smoothScroller)
    }

    private fun scrollTo(position: Int) {
        selectedPosition = position
        if (isFirstTime) {
            launch {
                if (!isListenerRegistered) {
                    delay(200)
                    isListenerRegistered = true
                    recyclerView.addOnScrollListener(listener)
                }

                normalScrolling(position)
            }
        } else {
            normalScrolling(position)
        }

    }

    protected fun scrollToPosition(position: Int) {
        launch {
            scrollTo(position + getDistance())
        }
    }
}