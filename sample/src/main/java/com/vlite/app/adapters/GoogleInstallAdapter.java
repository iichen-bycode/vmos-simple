package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.samplekit.adapters.BaseBindingAdapter;
import com.vlite.app.bean.GoogleInstallInfo;
import com.vlite.app.databinding.ItemGoogleAppInstallBinding;

import java.util.List;

public class GoogleInstallAdapter extends BaseBindingAdapter<GoogleInstallInfo, ItemGoogleAppInstallBinding> {

    public GoogleInstallAdapter(List<GoogleInstallInfo> data) {
        super(data);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemGoogleAppInstallBinding> holder, GoogleInstallInfo item, int position) {
        holder.binding.appNameTv.setText(item.getName());
        holder.binding.describeTv.setText(item.getDescribe());
        item.setPosition(position);
        if (item.getPackageInfo() == null) {
            holder.binding.packageNameTv.setVisibility(View.INVISIBLE);
        } else {
            holder.binding.packageNameTv.setVisibility(View.VISIBLE);
            holder.binding.packageNameTv.setText(item.getPackageInfo().packageName);
        }

    }

    @NonNull
    @Override
    public BindingHolder<ItemGoogleAppInstallBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemGoogleAppInstallBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
}
