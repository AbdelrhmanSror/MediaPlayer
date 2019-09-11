package com.example.mediaplayer

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * loading an image into imageView
 */
@BindingAdapter("imageUri")
fun setImageUri(imageView: ImageView, imageUri: String) {
    Glide.with(imageView.context).load(imageUri)
            /*.transform(new RoundedCorners(30))*/.apply(RequestOptions.circleCropTransform()
    ).into(imageView)

}

/**
 *  for formatting duration of audio file
 */
@BindingAdapter("duration")
fun setDuration(textView: TextView, milliSec: Long) {
    val minutes = milliSec.div(1000).div(60).toInt()
    val second = milliSec.div(1000).rem(60).toInt()
    val durationFormat = textView.context.resources.getString(R.string.duration_format, minutes, second)
    textView.text = durationFormat
}