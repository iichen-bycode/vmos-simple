package com.gmspace.app.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.gmspace.app.bean.AppItemEnhance;
import com.samplekit.adapters.BaseBindingAdapter;
import com.samplekit.bean.AppItem;
import com.gmspace.app.databinding.ItemAppBinding;
import com.gmspace.app.utils.GlideUtils;
import com.vlite.sdk.context.HostContext;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class AppItemAdapter extends BaseBindingAdapter<AppItemEnhance, ItemAppBinding> {

    public AppItemAdapter(List<AppItemEnhance> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemAppBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemAppBinding> holder, AppItemEnhance item, int position) {
        GlideUtils.loadFadeSkipCache(holder.binding.ivLogo, item == null ? null : item.getIconUri());
        holder.binding.tvAppName.setText(item == null ? null : item.getAppName());
    }
}
