package com.gmspace.app.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gmspace.sdk.proxy.GmSpaceHostContext;
import com.samplekit.adapters.BaseBindingAdapter;
import com.gmspace.app.bean.RunningInfo;
import com.gmspace.app.databinding.ItemRunningTaskCardBinding;
import com.gmspace.app.utils.GlideUtils;


import java.util.List;


public class RunningTaskCardAdapter extends BaseBindingAdapter<RunningInfo, ItemRunningTaskCardBinding> {

    private OnClickPlaceholderListener mClickPlaceholderListener;

    public void setOnClickPlaceholderListener(OnClickPlaceholderListener l) {
        this.mClickPlaceholderListener = l;
    }

    private String dimensionRatio = null;

    public RunningTaskCardAdapter(List<RunningInfo> data) {
        super(data);
        WindowManager wm = (WindowManager) GmSpaceHostContext.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        this.dimensionRatio = metrics.widthPixels + ":" + metrics.heightPixels;
    }

    @NonNull
    @Override
    public BindingHolder<ItemRunningTaskCardBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ItemRunningTaskCardBinding binding = ItemRunningTaskCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        final ViewGroup.LayoutParams lp = binding.ivSnapshot.getLayoutParams();
        if (lp instanceof ConstraintLayout.LayoutParams) {
            ((ConstraintLayout.LayoutParams) lp).dimensionRatio = dimensionRatio;
        }
        return new BindingHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemRunningTaskCardBinding> holder, RunningInfo item, int position) {
        holder.binding.spTitleIcon.setImageDrawable(item.getIcon());
        holder.binding.tvTitle.setText(item.getAppName());
        holder.binding.ivSnapshot.setBackgroundColor(item.getBackgroundColor());
        GlideUtils.loadFadeSkipCache(holder.binding.ivSnapshot, item.getSnapshot());

        holder.binding.placeholder.setOnClickListener(v -> {
            if (mClickPlaceholderListener != null) {
                mClickPlaceholderListener.onClickPlaceholder(position);
            }
        });
    }

    public interface OnClickPlaceholderListener {
        void onClickPlaceholder(int position);
    }

}
