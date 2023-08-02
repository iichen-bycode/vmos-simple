package com.vlite.app.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.samplekit.adapters.BaseBindingAdapter;
import com.vlite.app.R;
import com.vlite.app.bean.ProcessInfo;
import com.vlite.app.databinding.ItemProcessInfoBinding;
import com.vlite.app.utils.FileSizeFormat;

import java.util.List;

/**
 * 进程信息
 */
public class ProcessItemAdapter extends BaseBindingAdapter<ProcessInfo, ItemProcessInfoBinding> {
    private int ITEM_TYPE_HEADER = 1;
    private int ITEM_TYPE_INFO = 0;

    public ProcessItemAdapter(List<ProcessInfo> data) {
        super(data);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ITEM_TYPE_HEADER : ITEM_TYPE_INFO;
    }

    @NonNull
    @Override
    public BaseBindingAdapter.BindingHolder<ItemProcessInfoBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemProcessInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemProcessInfoBinding> holder, int position) {
        final int viewType = getItemViewType(position);
        if (ITEM_TYPE_HEADER == viewType) {
            holder.binding.tvPid.setText("PID");
            holder.binding.tvUid.setText("UID");
            holder.binding.tvProcessPss.setText("PSS");
            holder.binding.tvProcessName.setText("NAME");
            holder.binding.tvProcessName.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.itemView.getResources().getDimensionPixelSize(R.dimen.text_size_process_header));
            holder.binding.tvProcessName.setOnClickListener(null);
        } else {
            int dataPosition = position - 1;
            final ProcessInfo item = data.get(dataPosition);
            holder.binding.tvPid.setText(String.valueOf(item.pid));
            holder.binding.tvUid.setText(String.valueOf(item.uid));
            holder.binding.tvProcessName.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.itemView.getResources().getDimensionPixelSize(R.dimen.text_size_process_name));
            holder.binding.tvProcessPss.setText(FileSizeFormat.formatSize(item.pss));
            holder.binding.tvProcessName.setText(item.processName);
            holder.binding.tvProcessName.setOnClickListener(v -> {
                if (item.processName != null) {
                    Toast.makeText(v.getContext(), item.processName, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemProcessInfoBinding> holder, ProcessInfo item, int position) {

    }
}
