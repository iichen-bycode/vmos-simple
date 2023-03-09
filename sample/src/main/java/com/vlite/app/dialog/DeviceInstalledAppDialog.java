package com.vlite.app.dialog;

import android.content.pm.PackageInfo;

import java.util.List;

/**
 * 真机应用列表dialog
 */
public class DeviceInstalledAppDialog extends InstalledAppDialog {
    public DeviceInstalledAppDialog() {
        super(null);
    }

    public DeviceInstalledAppDialog(String title) {
        super(title);
    }

    @Override
    protected List<PackageInfo> getInstalledPackages() {
        return getContext().getPackageManager().getInstalledPackages(0);
    }
}
