package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.vlite.app.databinding.ItemAppBinding;
import com.vlite.app.utils.GlideUtils;
import com.vmos.samplekit.adapters.BaseBindingAdapter;
import com.vmos.samplekit.bean.AppItem;

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
        holder.binding.tvAppname.setText(item == null ? null : item.getAppName());
    }
}
