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

package com.example.mediaplayer.ui.chosenSong

import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.mediaplayer.R
import com.example.mediaplayer.extensions.twoDigitNumber
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.shared.Event
import com.example.mediaplayer.ui.chosenSong.adapter.ImageListAdapter
import com.example.mediaplayer.ui.chosenSong.adapter.SongListAdapter
import com.google.android.exoplayer2.Player


@BindingAdapter("app:songs")
fun setSongs(listView: RecyclerView, items: List<SongModel>?) {
    items?.let {
        (listView.adapter as SongListAdapter).submitList(items)
    }
}

@BindingAdapter("app:songsImage")
fun setSongsImage(listView: RecyclerView, items: List<String>?) {
    items?.let {
        (listView.adapter as ImageListAdapter).submitList(items)
    }
}

/**
 * binding adapter for changing the repeat button shape when user clicks on it
 */
@BindingAdapter("repeatMode")
fun adjustRepeat(imageButton: ImageButton, repeatMode: Int) {
    imageButton.apply {
        if (repeatMode == Player.REPEAT_MODE_ALL) {
            setImageResource(R.drawable.ic_repeat)

        } else {
            setImageResource(R.drawable.ic_repeat_transparent)
        }
    }
}

@BindingAdapter("setDuration")
fun setDuration(textView: TextView, duration: Long?) {
    with(textView) {
        duration?.let {
            val min = (duration / 1000).toFloat() / 60
            val sec = (min - min.toInt()) * 60
            textView.text = context.getString(R.string.duration_format, min.toInt().twoDigitNumber(), sec.toInt().twoDigitNumber())


        }

    }
}


/**
 * binding adapter for changing the shuffle button shape when user clicks on it
 */
@BindingAdapter("shuffleMode")
fun adjustShuffle(imageButton: ImageButton, enable: Boolean) {
    imageButton.apply {
        if (!enable) {
            setImageResource(R.drawable.ic_shuffle_transparent)
        } else {
            setImageResource(R.drawable.ic_shuffle)

        }
    }
}


@BindingAdapter("playPauseDrawable")
fun playPauseDrawable(imageButton: ImageButton, state: Boolean?) {
    imageButton.apply {
        state?.let {
            if (state) {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_play_media))
            } else {
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_pause_media))
            }

        }
    }
}

/**
 * binding adapter for changing the play pause button shape when user clicks on it
 * first parameter of pair indicate the current state of player if it is playing or paused
 * second parameter indicate if its initial state or not so if it was we will not start animation
 */
@BindingAdapter("playPauseAnimation")
fun playPauseAnimation(imageButton: ImageButton, state: Event<Boolean>?) {
    imageButton.apply {
        Log.v("buttonTag", "$tag")
        state?.let { event ->
            when (event.hasBeenHandled) {
                false -> {
                    val animatedVector = if (event.peekContent()) {
                        AnimatedVectorDrawableCompat.create(context, R.drawable.play_pause_media)
                    } else {
                        AnimatedVectorDrawableCompat.create(context, R.drawable.pause_play_media)
                    }
                    setImageDrawable(animatedVector)
                    animatedVector?.start()
                }
                else -> {
                    if (event.peekContent()) {
                        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_play_media))
                    } else {
                        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_pause_media))
                    }
                }
            }

        }

    }

}



@BindingAdapter("setInitialFavourite")
fun setInitialFavourite(imageButton: ImageButton, isFavourite: Boolean) {
    if (!isFavourite) {
        imageButton.setImageResource(R.drawable.ic_favourite_stroke)
    } else {
        imageButton.setImageResource(R.drawable.ic_favourite)
    }
}


@BindingAdapter("Progress")
fun setProgress(textView: TextView, progress: Long) {
    val min = (progress / 60).toInt().twoDigitNumber()
    val sec = (progress % 60).toInt().twoDigitNumber()
    textView.text = textView.context.getString(R.string.duration_format, min, sec)
}

@BindingAdapter("Max")
fun setMax(seekBar: SeekBar, max: Long) {

    seekBar.max = (max / 1000).toInt()
}



