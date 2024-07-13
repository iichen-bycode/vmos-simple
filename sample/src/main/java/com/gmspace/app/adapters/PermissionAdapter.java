package com.gmspace.app.adapters;

import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.samplekit.adapters.BaseBindingAdapter;
import com.gmspace.app.bean.AppPermissionInfo;
import com.gmspace.app.databinding.ItemAppPermissionBinding;

import java.util.List;

public class PermissionAdapter extends BaseBindingAdapter<AppPermissionInfo, ItemAppPermissionBinding> {

    public PermissionAdapter(@Nullable List<AppPermissionInfo> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemAppPermissionBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemAppPermissionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemAppPermissionBinding> holder, AppPermissionInfo item, int position) {
        holder.binding.tvPermissionDisplayName.setText(item.getPermissionDisplayName());
        holder.binding.tvPermissionName.setText(item.getPermission());
        holder.binding.swPermissionStatus.setChecked(item.getPermissionResult() == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemAppPermissionBinding> holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            final AppPermissionInfo item = getData().get(position);
            holder.binding.swPermissionStatus.setChecked(item.getPermissionResult() == PackageManager.PERMISSION_GRANTED);
        }
    }
}
