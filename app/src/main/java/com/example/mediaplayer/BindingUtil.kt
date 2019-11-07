package com.example.mediaplayer

import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.Player

/**
 * loading an image into imageView
 * if there is no album for this audio replace it with default one
 */
@BindingAdapter("imageUri")
fun setImageUri(imageView: ImageView, imageUri: String?) {
    Log.v("imageadapterpor", "hi")

    Glide.with(imageView.context)
            .load(imageUri
                    ?: R.drawable.default_image).transform(RoundedCorners(40).apply { RequestOptions.centerCropTransform() }
            ).into(imageView)
}

/**
 * loading an image into imageView
 * if there is no album for this audio replace it with default one
 */
@BindingAdapter("circularImageUri")
fun setCircularImageUri(imageView: ImageView, imageUri: String?) {
    Glide.with(imageView.context)
            .load(imageUri
                    ?: R.drawable.default_image).apply(RequestOptions.circleCropTransform().apply { RequestOptions.centerCropTransform() }).into(imageView)
}


/**
 *  for formatting duration of audio file
 */
@BindingAdapter("duration")
fun setDuration(textView: TextView, milliSec: Long) {
    val minutes = milliSec.div(1000).div(60).toInt()
    val second = milliSec.div(1000).rem(60).toInt()
    val durationFormat = textView.context.resources.getString(R.string.duration_format, minutes.twoDigitNumber(), second.twoDigitNumber())
    textView.text = durationFormat
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

/**
 * binding adapter for changing the play pause button shape when user clicks on it
 */
@BindingAdapter("playPauseOption")
fun playPauseAnimation(imageButton: ImageButton, playing: Boolean) {
    imageButton.apply {
        val animatedVector = if (playing) {
            AnimatedVectorDrawableCompat.create(context, R.drawable.play_pause_media)

        } else {
            AnimatedVectorDrawableCompat.create(context, R.drawable.pause_play_media)
        }
        setImageDrawable(animatedVector)
        animatedVector?.start()

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


