package com.vlite.app.sample;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tencent.mmkv.MMKV;
import com.vlite.app.bean.AppItem;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.model.DeviceEnvInfo;
import com.vlite.sdk.utils.BitmapUtils;
import com.vlite.sdk.utils.GsonUtils;

import java.io.File;

public class SampleUtils {
    private static final MMKV deviceMMKV = MMKV.mmkvWithID("virtual_device");

    public static DeviceEnvInfo getVirtualDeviceInfo() {
        final String json = deviceMMKV.getString("virtual_device", null);
        AppLogger.d(json);
        return GsonUtils.toObject(json, DeviceEnvInfo.class);
    }

    public static void putVirtualDeviceInfo(DeviceEnvInfo deviceInfo) {
        deviceMMKV.putString("virtual_device", GsonUtils.toJson(deviceInfo));
    }

    public static AppItem newAppItem(PackageManager pm, PackageInfo pkg) {
        final Intent launchIntent = VLite.get().getLaunchIntentForPackage(pkg.packageName);
        if (launchIntent != null) {
            final AppItem it = new AppItem();
            it.setPackageName(pkg.packageName);
            it.setAppName(pkg.applicationInfo.loadLabel(pm).toString());
            it.setIconUri(getIconCacheUri(pm, pkg.versionCode, pkg.applicationInfo));
            return it;
        }
        return null;
    }

    public static String getIconCacheUri(PackageManager pm, int versionCode, ApplicationInfo info) {
        final File iconDir = new File(HostContext.getContext().getCacheDir(), "icon_cache");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        final File iconFile = new File(iconDir, info.packageName + "_" + versionCode + ".png");
        if (!iconFile.exists()) {
            BitmapUtils.toFile(BitmapUtils.toBitmap(info.loadIcon(pm)), iconFile.getAbsolutePath());
        }
        return iconFile.getAbsolutePath();
    }
}
