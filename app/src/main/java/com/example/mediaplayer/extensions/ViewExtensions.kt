package com.example.mediaplayer.extensions

import android.widget.ImageButton
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.mediaplayer.R


//start animation of like or dislike audio
fun ImageButton.startFavouriteAnimation(addToFavourite: Boolean) {
    this.apply {
        val animatedVector = if (addToFavourite) {
            AnimatedVectorDrawableCompat.create(context, R.drawable.ic_favourite)

        } else {
            AnimatedVectorDrawableCompat.create(context, R.drawable.ic_favourite_stroke)
        }
        setImageDrawable(animatedVector)
        animatedVector?.start()
    }

}

