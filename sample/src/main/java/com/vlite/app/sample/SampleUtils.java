package com.vlite.app.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.tencent.mmkv.MMKV;
import com.vlite.app.bean.AppItem;
import com.vlite.app.utils.DialogAsyncTask;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.model.DeviceEnvInfo;
import com.vlite.sdk.utils.BitmapUtils;
import com.vlite.sdk.utils.GsonUtils;

import java.io.File;

public class SampleUtils {

    public static AppItem newAppItem(PackageManager pm, PackageInfo pkg) {
//        final Intent launchIntent = VLite.get().getLaunchIntentForPackage(pkg.packageName);
        final ActivityInfo launchIntent = VLite.get().getLaunchActivityInfoForPackage(pkg.packageName);
        if (launchIntent != null) {
            final AppItem it = new AppItem();
            it.setPackageName(pkg.packageName);
            it.setAppName(pkg.applicationInfo.loadLabel(pm).toString());
            it.setIconUri(getIconCacheUri(pm, pkg.versionCode, pkg.applicationInfo,launchIntent.loadIcon(pm)));
            return it;
        }
        return null;
    }

    public static String getIconCacheUri(PackageManager pm, int versionCode, ApplicationInfo info, Drawable drawable) {
        final File iconDir = new File(HostContext.getContext().getCacheDir(), "icon_cache");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        final File iconFile = new File(iconDir, info.packageName + "_" + versionCode + ".png");
        if (!iconFile.exists()) {
            BitmapUtils.toFile(BitmapUtils.toBitmap(drawable), iconFile.getAbsolutePath());
        }
        return iconFile.getAbsolutePath();
    }

    public static File getSnapshotCacheFile(String packageName) {
        final File dir = new File(HostContext.getContext().getCacheDir(), "snapshot_cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, packageName + ".jpg");
    }


    /**
     * 卸载应用
     *
     */
    public static void showUninstallAppDialog(Context context, String appName, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle("卸载应用")
                .setMessage("确定要卸载 " + appName + " 吗？")
                .setPositiveButton("卸载", listener)
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

}
