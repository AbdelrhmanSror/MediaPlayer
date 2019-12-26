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
    private var isLocked = false
    private var selectedPosition = 0
    private var isListenerRegistered = false
    var speed: Float = 8f//default is 25f (bigger = slower)
    abstract fun setCurrentSelectedPosition(position: Int)
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
                    if (shouldScroll(selectedPosition)) {
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
                if (shouldScroll(selectedPosition)) {
                    scrollTo(selectedPosition)
                } else {
                    unRegisterScrollingListener(recyclerView)
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


    private fun startSmoothScrolling(position: Int) {
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
        launch {
            listener.registerScrollingListener(recyclerView)
            startSmoothScrolling(selectedPosition)
        }
    }

    private fun RecyclerView.OnScrollListener.registerScrollingListener(recyclerView: RecyclerView) {
        if (!isListenerRegistered) {
            isListenerRegistered = true
            recyclerView.addOnScrollListener(this)
        }
    }

    private fun RecyclerView.OnScrollListener.unRegisterScrollingListener(recyclerView: RecyclerView) {
        isListenerRegistered = false
        recyclerView.removeOnScrollListener(this)
    }

    private suspend fun getDistance(): Int {
        return if (distance == -1) {
            delay(300)
            val firstPosition = firstVisibleItemPosition()
            val lastPosition = lastVisibleItemPosition()
            distance = abs((lastPosition - firstPosition) / 2)
            distance
        } else {
            distance
        }
    }

    private fun shouldScroll(position: Int): Boolean {
        if (position.coerceIn(firstVisibleItemPosition(), lastVisibleItemPosition()) != position) {
            return true
        }
        return false
    }

    /**
     * [scrollAnyWay] if true will try to scroll if even the item is visible otherwise will do check before scrolling
     * [position] u want to scroll to
     */
    protected fun scrollToPosition(position: Int, scrollAnyWay: Boolean = false) {
        launch {
            val distance = getDistance()
            if (!scrollAnyWay) {
                if (shouldScroll(position + distance)) {
                    scrollTo(position + distance)
                }
            } else {
                scrollTo(position + distance)
            }


        }
    }
}