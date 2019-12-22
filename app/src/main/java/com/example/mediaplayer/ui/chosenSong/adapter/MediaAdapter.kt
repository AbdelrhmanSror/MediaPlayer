package com.example.mediaplayer.ui.chosenSong.adapter

import android.util.DisplayMetrics
import androidx.recyclerview.widget.*
import com.example.mediaplayer.shared.CustomScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

abstract class MediaAdapter<VH : RecyclerView.ViewHolder, T>(private val recyclerView: RecyclerView, private val layoutManager: LinearLayoutManager, diffUtil: DiffUtil.ItemCallback<T>) :
        ListAdapter<T, VH>(diffUtil), CoroutineScope by CustomScope(Dispatchers.Main) {
    private var distance: Int = -1
    private var isSnapAttached = false
    private val snapHelper = LinearSnapHelper()
    private var isFirstTime = true
    private var isLocked = false
    private var selectedPosition = 0
    private var isListenerRegistered = false
    var speed: Float = 8f//default is 25f (bigger = slower)
    abstract fun setCurrentSelectedPosition(position: Int, scrollEnabled: Boolean)

    private fun firstVisibleItemPosition() = layoutManager.findFirstVisibleItemPosition()
    private fun lastVisibleItemPosition() = layoutManager.findLastVisibleItemPosition()

    init {
        snapHelper.attachToRecyclerView(recyclerView)
        isSnapAttached = true
    }

    private val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!isLocked) {
                launch {
                    isLocked = true
                    delay(500)
                    if (selectedPosition.coerceIn(firstVisibleItemPosition(), lastVisibleItemPosition()) != selectedPosition) {
                        scrollTo(selectedPosition)
                    }
                    isLocked = false
                }
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                /**
                 * if the scroller did not scroll to the selected position we scroll again until we get it right
                 */
                if (selectedPosition.coerceIn(firstVisibleItemPosition(), lastVisibleItemPosition()) != selectedPosition) {
                    scrollTo(selectedPosition)
                } else {
                    if (isFirstTime)
                        isFirstTime = false
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
                return speed / displayMetrics.densityDpi
            }
        }
    }


    private fun normalScrolling(position: Int) {
        //if position was the first or last then just scroll and disable snaphelper
        if (position <= 1 || position == itemCount - 1) {
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
        layoutManager.startSmoothScroll(smoothScroller)


    }

    /**
     *will make sure that number is in range of minNum to excluding maxNum
     */
    private fun Int.approximate(minNum: Int, maxNum: Int): Int {
        return when {
            this < minNum -> minNum
            this == maxNum -> maxNum - 1
            else -> this
        }
    }

    private fun scrollTo(position: Int) {
        selectedPosition = position.approximate(0, itemCount)
        if (isFirstTime) {
            launch {
                if (!isListenerRegistered) {
                    delay(200)
                    isListenerRegistered = true
                    recyclerView.addOnScrollListener(listener)
                }

                normalScrolling(selectedPosition)
            }
        } else {
            normalScrolling(selectedPosition)
        }

    }


    private suspend fun getDistance(): Int {
        return if (distance == -1) {
            delay(100)
            val firstPosition = firstVisibleItemPosition()
            val lastPosition = lastVisibleItemPosition()
            distance = abs((lastPosition - firstPosition) / 2)
            distance
        } else {
            distance
        }
    }

    protected fun scrollToPosition(position: Int) {
        launch {
            scrollTo(abs(position + getDistance()))
        }
    }
}