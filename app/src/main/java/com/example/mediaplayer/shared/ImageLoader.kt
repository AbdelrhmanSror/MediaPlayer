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

package com.example.mediaplayer.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import com.example.mediaplayer.R
import java.io.FileNotFoundException

@Suppress("BlockingMethodInNonBlockingContext")
object ImageLoader {
    @Suppress("DEPRECATION")
    fun getImageBitmap(context: Context, uri: Uri?): Bitmap {

        if (uri == null) {
            return BitmapFactory.decodeResource(context.resources, R.drawable.default_image)
        }
        return try {
            if (isQOrLater()) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (exp: FileNotFoundException) {
            BitmapFactory.decodeResource(context.resources, R.drawable.default_image)

        }

    }
}