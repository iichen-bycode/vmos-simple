package com.vlite.app.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.List;

public abstract class BaseBindingAdapter<T, B extends ViewBinding> extends RecyclerView.Adapter<BaseBindingAdapter.BindingHolder<B>> {
    protected List<T> data;
    protected OnItemClickListener mOnItemClickListener = null;
    protected OnItemLongClickListener mOnItemLongClickListener = null;

    public BaseBindingAdapter(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<B> holder, int position) {
        onBindViewHolder(holder, data.get(position), position);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition()));
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                mOnItemLongClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                return true;
            });
        }
    }

    public abstract void onBindViewHolder(@NonNull BindingHolder<B> holder, T item, int position);

    public static class BindingHolder<B extends ViewBinding> extends RecyclerView.ViewHolder {
        B binding;

        public BindingHolder(@NonNull B binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
