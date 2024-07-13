package com.gmspace.app.dialog;

import android.content.pm.PackageInfo;

import androidx.viewbinding.ViewBinding;

import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.samplekit.adapters.BaseBindingAdapter;
import com.samplekit.bean.InstalledInfo;
import com.samplekit.dialog.InstalledAppDialog;
import com.gmspace.app.adapters.VmInstalledAppAdapter;
import java.util.List;

/**
 * 虚拟机应用列表dialog
 */
public class VmInstalledAppDialog extends InstalledAppDialog {
    private VmInstalledAppAdapter mAdapter;

    public VmInstalledAppDialog() {
        super(null);
    }

    public VmInstalledAppDialog(String title) {
        super(title);
    }

    @Override
    protected List<PackageInfo> getInstalledPackages() {
        return GmSpaceUtils.getInstalledPackages(0);
    }

    @Override
    protected BaseBindingAdapter<InstalledInfo, ? extends ViewBinding> newRecyclerViewAdapter(List<InstalledInfo> result) {
        mAdapter = new VmInstalledAppAdapter(result);
        return mAdapter;
    }

    @Override
    protected void applyInstalledAdapterConfig(BaseBindingAdapter<InstalledInfo, ? extends ViewBinding> installedAdapter) {
        mAdapter.setOnItemClickListener((view, position) -> {
            final OnClickInstalledItemListener listener = getOnClickInstalledItemListener();
            if (listener != null) {
                listener.onClickInstalledItem(mAdapter.getData().get(position), position);
            }
        });
        mAdapter.setOnItemLongClickListener(null);
    }

    @Override
    protected int getSpanCount() {
        return 1;
    }

    public void removeItem(int index) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.getData().remove(index);
        mAdapter.notifyDataSetChanged();
    }
}
