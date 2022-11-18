package com.vlite.app.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

public class GlideUtils {
    public static void loadFade(ImageView imageView, String url) {
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(imageView).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .into(new DrawableImageViewTarget(imageView).waitForLayout());
    }
}
