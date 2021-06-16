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

package com.example.mediaplayer

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


/**
 * loading an image into imageView
 * if there is no album for this audio replace it with default one
 */
@BindingAdapter("circularImageUri")
fun setCircularImageUri(imageView: ImageView, imageUri: String?) {
    Glide.with(imageView.context)
            .load(imageUri).error(R.drawable.default_image).transform(RoundedCorners(40)).apply(RequestOptions.circleCropTransform().apply { RequestOptions.centerCropTransform() }).into(imageView)
}

/**
 * loading an image into imageView
 * if there is no album for this audio replace it with default one
 */
@BindingAdapter("imageUri")
fun setImageUri(imageView: ImageView, imageUri: String?) {
    Glide.with(imageView.context)
            .load(imageUri).error(R.drawable.default_image).transform(RoundedCorners(1).apply { RequestOptions.centerCropTransform() }
            ).into(imageView)
}