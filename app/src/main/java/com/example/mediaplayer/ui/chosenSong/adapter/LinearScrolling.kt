package com.example.mediaplayer.ui.chosenSong.adapter

import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.example.mediaplayer.shared.CustomScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


open class LinearScrolling(private val recyclerView: RecyclerView, private val itemCount: Int) : CoroutineScope by CustomScope((Dispatchers.Main)) {
    private var isSnapAttached = false
    private val snapHelper = LinearSnapHelper()
    private var isFirstTime = true
    private var selectedPosition = 0
    private var isListenerRegistered = false
    private val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
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

    companion object {
        private const val MILLISECONDS_PER_INCH = 8f //default is 25f (bigger = slower)
    }

    init {
        snapHelper.attachToRecyclerView(recyclerView)
        isSnapAttached = true

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

    fun scrollTo(position: Int) {
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

}