package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vlite.app.bean.InstalledInfo;
import com.vlite.app.databinding.ItemInstalledBinding;

import java.util.List;

public class InstalledAdapter extends BaseBindingAdapter<InstalledInfo, ItemInstalledBinding> {
    public InstalledAdapter(@Nullable List<InstalledInfo> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemInstalledBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemInstalledBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemInstalledBinding> holder, InstalledInfo item, int position) {
        holder.binding.tvAppname.setText(item.getAppName());
        holder.binding.tvCreateTime.setVisibility(View.VISIBLE);
        holder.binding.tvCreateTime.setText(item.getPackageName());
        holder.binding.ivLogo.setImageDrawable(item.getIcon());
    }
}
