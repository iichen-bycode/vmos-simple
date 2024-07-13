package com.gmspace.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.samplekit.adapters.BaseBindingAdapter;
import com.samplekit.bean.AppItem;
import com.gmspace.app.databinding.ItemAppBinding;
import com.gmspace.app.utils.GlideUtils;

import java.util.List;

public class AppItemAdapter extends BaseBindingAdapter<AppItem, ItemAppBinding> {

    public AppItemAdapter(List<AppItem> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemAppBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemAppBinding> holder, AppItem item, int position) {
        GlideUtils.loadFadeSkipCache(holder.binding.ivLogo, item == null ? null : item.getIconUri());
        holder.binding.tvAppName.setText(item == null ? null : item.getAppName());
    }
}
