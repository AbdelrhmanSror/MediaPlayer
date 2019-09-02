package com.example.mediaplayer;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class bindingUtil {
    @BindingAdapter("imageUri")
    public static void setImageUri(ImageView imageView, String imageUri)
    {
        Glide.with(imageView.getContext()).load(imageUri)
                .transform(new RoundedCorners(10)).into(imageView);

    }
}
