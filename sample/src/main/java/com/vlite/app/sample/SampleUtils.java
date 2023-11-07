package com.vlite.app.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.samplekit.bean.AppItem;
import com.vlite.app.utils.RuntimeUtils;
import com.vlite.app.view.LauncherAdaptiveIconDrawable;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.utils.BitmapUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SampleUtils {
    public static final String GP_PACKAGE_NAME = "com.android.vending";
    public static final String GMS_PACKAGE_NAME = "com.google.android.gms";
    public static final String GSF_PACKAGE_NAME = "com.google.android.gsf";

    public static String eventToPrintString(Bundle bundle) {
        final String[] sortOrder = {BinderEvent.KEY_EVENT_ID, BinderEvent.KEY_PACKAGE_NAME, BinderEvent.KEY_PACKAGE_NAME_ARRAY,
                BinderEvent.KEY_PROCESS_NAME, BinderEvent.KEY_CLASS_NAME, BinderEvent.KEY_METHOD_NAME, BinderEvent.KEY_OBJECT_ID};
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
        final File iconDir = new File(HostContext.getContext().getCacheDir(), "icon_cache_v2");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        final File iconFile = new File(iconDir, packageName + "_" + versionCode + "_" + imageKey + ".png");
        if (!iconFile.exists()) {
            BitmapUtils.toFile(BitmapUtils.toBitmap(convertLauncherDrawable(drawable)), iconFile.getAbsolutePath());
        }
        return iconFile.getAbsolutePath();
    }

    public static Drawable convertLauncherDrawable(final Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
            return new LauncherAdaptiveIconDrawable((AdaptiveIconDrawable) drawable);
        } else {
            return drawable;
        }
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


    private static void addAbiInfo(String apkFilePath ,Set<String> supportedABIs){
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("lib/")) {
                    // 获取库文件的架构类型
                    String[] parts = name.split("/");
                    if (parts.length >= 3) {
                        String abi = parts[1];
                        if (!TextUtils.isEmpty(abi)) {
                            supportedABIs.add(abi);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipFile != null) zipFile.close();
            } catch (IOException ignored) {
            }
        }
    }
    public static boolean isApkSupportedHost(String apkFilePath) {
        File file = new File(apkFilePath);
        final Set<String> supportedABIs = new HashSet<>();
        if(file.isDirectory()){
            File[] files = file.listFiles(child -> child.getName().endsWith(".apk"));
            for (File apkFile : files) {
                addAbiInfo(apkFile.getAbsolutePath(),supportedABIs);
            }
        }else {
            addAbiInfo(apkFilePath,supportedABIs);
        }
        if (RuntimeUtils.is64bit()) {
            return supportedABIs.isEmpty() || supportedABIs.contains("arm64-v8a");
        } else {
            return supportedABIs.isEmpty() || supportedABIs.contains("armeabi-v7a") || supportedABIs.contains("armeabi");
        }
    }

    public static int getMinSdkVersion(String filePath,Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageArchiveInfo(filePath, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return info.applicationInfo.minSdkVersion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isMicroG(PackageInfo info) {
        if (info != null) {
            final ProviderInfo[] providers = info.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    if (provider.name.startsWith("org.microg")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
