package com.example.mediaplayer.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.mediaplayer.R

object ImageLoader {
    fun getImageBitmap(context: Context, uri: Uri?): Bitmap {
        if (uri == null) {
            return BitmapFactory.decodeResource(context.resources, R.drawable.default_image)
        }
        return if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }
}