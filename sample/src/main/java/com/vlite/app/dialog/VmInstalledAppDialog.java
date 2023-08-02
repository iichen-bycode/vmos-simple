package com.vlite.app.dialog;

import android.content.pm.PackageInfo;

import androidx.viewbinding.ViewBinding;

import com.samplekit.adapters.BaseBindingAdapter;
import com.samplekit.bean.InstalledInfo;
import com.samplekit.dialog.InstalledAppDialog;
import com.vlite.app.adapters.VmInstalledAppAdapter;
import com.vlite.sdk.VLite;

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
        return VLite.get().getInstalledPackages(0);
    }

    @Override
    protected BaseBindingAdapter<InstalledInfo, ? extends ViewBinding> newRecyclerViewAdapter(List<InstalledInfo> result) {
        mAdapter = new VmInstalledAppAdapter(result);
        return mAdapter;
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
