package com.vlite.app.dialog;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.vlite.app.bean.GoogleInstallInfo;
import com.vlite.app.sample.SampleUtils;
import com.vlite.sdk.VLite;

import java.util.ArrayList;
import java.util.List;

public class MicroGInstallDialog extends InstallKitDialog {

    public MicroGInstallDialog(@NonNull Context context) {
        super(context,"microG");
    }

    @Override
    protected List<GoogleInstallInfo> getInstallKitInfos() {
        //此下载地址仅用于DEMO测试用，请勿在生产环境使用
        final List<GoogleInstallInfo> list = new ArrayList<>();
        list.add(new GoogleInstallInfo(SampleUtils.GP_PACKAGE_NAME,"Google Play商店",
                "https://files.vmos.pro/sample/gpstore.apk"));
        list.add(new GoogleInstallInfo(SampleUtils.GMS_PACKAGE_NAME,"microG",
                "https://files.vmos.pro/sample/microg-233013058.apk"));
        return list;
    }

    @Override
    protected void onPreinstallGoogleServiceKit() {
        // 安装microG之前要先卸载谷歌服务
        final PackageInfo packageInfo = VLite.get().getPackageInfo(SampleUtils.GMS_PACKAGE_NAME, PackageManager.GET_PROVIDERS);
        if (!SampleUtils.isMicroG(packageInfo)){
            VLite.get().uninstallPackage(SampleUtils.GMS_PACKAGE_NAME);
            VLite.get().uninstallPackage(SampleUtils.GSF_PACKAGE_NAME);
        }
    }

    @Override
    protected PackageInfo getPackageInfo(String packageName) {
        final PackageInfo packageInfo = VLite.get().getPackageInfo(packageName, PackageManager.GET_PROVIDERS);
        if (SampleUtils.GMS_PACKAGE_NAME.equals(packageName)) {
            return SampleUtils.isMicroG(packageInfo) ? packageInfo : null;
        } else {
            return packageInfo;
        }
    }
}
