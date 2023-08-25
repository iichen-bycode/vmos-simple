package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.samplekit.adapters.BaseBindingAdapter;
import com.vlite.app.bean.AppPermissionInfo;
import com.vlite.app.databinding.ItemInstallerPermissionBinding;

import java.util.List;

public class InstallerPermissionAdapter extends BaseBindingAdapter<AppPermissionInfo, ItemInstallerPermissionBinding> {

    public InstallerPermissionAdapter(@Nullable List<AppPermissionInfo> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemInstallerPermissionBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemInstallerPermissionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemInstallerPermissionBinding> holder, AppPermissionInfo item, int position) {
        holder.binding.tvPermissionDisplayName.setText(item.getPermissionDisplayName());
        holder.binding.tvPermissionDesc.setText(item.getPermissionDescription());
    }

}
