package com.vlite.app.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.samplekit.bean.AppItem;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.utils.BitmapUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleUtils {

    public static String eventToPrintString(Bundle bundle) {
        final String[] sortOrder = {BinderEvent.KEY_EVENT_ID, BinderEvent.KEY_PACKAGE_NAME, BinderEvent.KEY_PACKAGE_NAME_ARRAY,
                BinderEvent.KEY_PROCESS_NAME, BinderEvent.KEY_CLASS_NAME, BinderEvent.KEY_METHOD_NAME};
        final Map<String, Integer> sortOrderMap = new HashMap<>();
        for (int i = sortOrder.length - 1; i >= 0; i--) {
            sortOrderMap.put(sortOrder[i], i);
        }
        return toPrintString(bundle, (s1, s2) -> {
            Integer index1 = sortOrderMap.get(s1);
            Integer index2 = sortOrderMap.get(s2);
            // 如果其中一个字符串不在指定顺序中，则返回它们的自然顺序比较结果
            if (index1 == null && index2 == null) {
                return s1.compareTo(s2);
            } else if (index1 == null) {
                return 1; // s1 不在指定顺序中，放在后面
            } else if (index2 == null) {
                return -1; // s2 不在指定顺序中，放在后面
            }
            return Integer.compare(index1, index2);
        });
    }

    public static String toPrintString(Bundle bundle, Comparator<String> comparator) {
        if (bundle == null) {
            return "null";
        }
        final StringBuilder builder = new StringBuilder("\n{");
        final List<String> keys = new ArrayList<>(bundle.keySet());
        if (comparator != null) {
            Collections.sort(keys, comparator);
        }
        for (String key : keys) {
            builder.append("\t").append(key).append(" = ");
            final Object value = bundle.get(key);
            if (value != null && value.getClass().isArray()) {
                builder.append(Arrays.toString((Object[]) value));
            } else {
                builder.append(value);
            }
            builder.append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

    public static AppItem newAppItem(PackageManager pm, PackageInfo pkg) {
//        final Intent launchIntent = VLite.get().getLaunchIntentForPackage(pkg.packageName);
        final ActivityInfo launchIntent = VLite.get().getLaunchActivityInfoForPackage(pkg.packageName);
        if (launchIntent != null) {
            final AppItem it = new AppItem();
            it.setVersionCode(pkg.versionCode);
            it.setVersionName(pkg.versionName);
            it.setPackageName(pkg.packageName);
            it.setAppName(pkg.applicationInfo.loadLabel(pm).toString());
            it.setIconUri(getIconCacheUri(pkg.packageName, pkg.versionCode, launchIntent.loadIcon(pm), launchIntent.name));
            return it;
        }
        return null;
    }

    public static String getIconCacheUri(String packageName, int versionCode, Drawable drawable) {
        return getIconCacheUri(packageName, versionCode, drawable, "");
    }

    public static String getIconCacheUri(String packageName, int versionCode, Drawable drawable, String imageKey) {
        final File iconDir = new File(HostContext.getContext().getCacheDir(), "icon_cache");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        final File iconFile = new File(iconDir, packageName + "_" + versionCode + "_" + imageKey + ".png");
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
