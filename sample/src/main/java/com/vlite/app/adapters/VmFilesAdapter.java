package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.vlite.app.R;
import com.vlite.app.bean.VmFileItem;
import com.vlite.app.databinding.ItemVmFileBinding;
import com.vlite.app.utils.FileSizeFormat;

import java.util.List;

public class VmFilesAdapter extends BaseBindingAdapter<VmFileItem, ItemVmFileBinding> {

    public VmFilesAdapter(List<VmFileItem> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemVmFileBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemVmFileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemVmFileBinding> holder, VmFileItem item, int position) {
        holder.binding.tvFileName.setText(item.getName());
        if (item.isDirectory()) {
            holder.binding.ivTypeIcon.setImageResource(R.drawable.ic_folder);
            holder.binding.ivTypeClickable.setVisibility(View.VISIBLE);
            holder.binding.tvFileSize.setVisibility(View.GONE);
        } else {
            holder.binding.ivTypeIcon.setImageResource(R.drawable.ic_file);
            holder.binding.ivTypeClickable.setVisibility(View.GONE);
            holder.binding.tvFileSize.setVisibility(View.VISIBLE);
            holder.binding.tvFileSize.setText(FileSizeFormat.formatSize(item.getSize()));
        }
        holder.binding.getRoot().setAlpha(item.isDisabled() ? 0.5f : 1f);
    }

}
