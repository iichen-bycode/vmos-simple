package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vlite.app.bean.InstalledInfo;
import com.vlite.app.databinding.ItemVmInstalledAppBinding;

import java.util.List;

public class VmInstalledAppAdapter extends BaseBindingAdapter<InstalledInfo, ItemVmInstalledAppBinding> {
    public VmInstalledAppAdapter(@Nullable List<InstalledInfo> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemVmInstalledAppBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemVmInstalledAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemVmInstalledAppBinding> holder, InstalledInfo item, int position) {
        holder.binding.tvAppName.setText(item.getAppName());
        holder.binding.tvAppPackageName.setText(item.getPackageName());
        holder.binding.ivAppIcon.setImageDrawable(item.getIcon());
    }
}
