package com.example.mediaplayer

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("imageUri")
fun setImageUri(imageView: ImageView, imageUri: String) {
    Glide.with(imageView.context).load(imageUri)
            /*.transform(new RoundedCorners(30))*/.apply(RequestOptions.circleCropTransform()
    ).into(imageView)

}