package com.example.mediaplayer

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


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



