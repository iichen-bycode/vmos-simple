package com.vlite.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.samplekit.adapters.BaseBindingAdapter;
import com.vlite.app.bean.FloatMenuItem;
import com.vlite.app.databinding.ItemFloatMenuBinding;

import java.util.List;

public class FloatMenuAdapter extends BaseBindingAdapter<FloatMenuItem, ItemFloatMenuBinding> {

    public FloatMenuAdapter(List<FloatMenuItem> data) {
        super(data);
    }

    @NonNull
    @Override
    public BindingHolder<ItemFloatMenuBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemFloatMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemFloatMenuBinding> holder, FloatMenuItem item, int position) {
        holder.binding.ivLogo.setImageResource(item.getIcon());
        holder.binding.tvName.setText(item.getName());
    }
}
