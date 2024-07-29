package com.gmspace.app.utils;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.net.URL;

import androidx.annotation.Nullable;

public class GlideUtils {
    public static void loadFade(ImageView imageView, String url) {
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(imageView).load(url)
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .into(new DrawableImageViewTarget(imageView).waitForLayout());
    }

    public static void loadFadeSkipCache(ImageView imageView, String url) {
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(imageView).load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .into(new DrawableImageViewTarget(imageView).waitForLayout());
    }

    public static void loadFadeSkipCache(ImageView imageView, Uri uri) {
        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        Glide.with(imageView).load(uri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Drawable> target, boolean b) {
                        Log.d("iichen",">>>>>>>>>>>>>>>>>>>>失败 " + e.getMessage());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable drawable, Object o, Target<Drawable> target, DataSource dataSource, boolean b) {
                        Log.d("iichen",">>>>>>>>>>>>>>>>>>>>cg " + drawable);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .into(new DrawableImageViewTarget(imageView).waitForLayout());
    }
}
