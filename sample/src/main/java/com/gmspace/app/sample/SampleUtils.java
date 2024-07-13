package com.gmspace.app.sample;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.gmspace.app.BuildConfig;
import com.gmspace.sdk.GmSpaceEvent;
import com.gmspace.sdk.GmSpaceInstallConfig;
import com.gmspace.sdk.GmSpaceObject;
import com.gmspace.sdk.GmSpaceResultParcel;
import com.gmspace.sdk.proxy.GmSpaceBitmapUtils;
import com.gmspace.sdk.proxy.GmSpaceHostContext;
import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.samplekit.bean.AppItem;
import com.samplekit.utils.FilenameUtils;
import com.gmspace.app.utils.RuntimeUtils;
import com.gmspace.app.view.LauncherAdaptiveIconDrawable;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

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
    public static final String PLUGIN_PACKAGE_NAME = BuildConfig.SERVER_PACKAGE_NAME+".arm32";

    private static GmSpaceInstallConfig mGlobalInstallConfig = new GmSpaceInstallConfig();

    public static String eventToPrintString(Bundle bundle) {
        final String[] sortOrder = {GmSpaceEvent.KEY_EVENT_ID, GmSpaceEvent.KEY_PACKAGE_NAME, GmSpaceEvent.KEY_PACKAGE_NAME_ARRAY,
                GmSpaceEvent.KEY_PROCESS_NAME, GmSpaceEvent.KEY_CLASS_NAME, GmSpaceEvent.KEY_METHOD_NAME, GmSpaceEvent.KEY_OBJECT_ID};
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
        final ActivityInfo launchIntent = GmSpaceUtils.getLaunchActivityInfoForPackage(pkg.packageName);
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
        final File iconDir = new File(GmSpaceHostContext.getContext().getCacheDir(), "icon_cache_v2");
        if (!iconDir.exists()) {
            iconDir.mkdirs();
        }
        final File iconFile = new File(iconDir, packageName + "_" + versionCode + "_" + imageKey + ".png");
        if (!iconFile.exists()) {
            GmSpaceBitmapUtils.toFile(GmSpaceBitmapUtils.toBitmap(convertLauncherDrawable(drawable)), iconFile.getAbsolutePath());
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
        final File dir = new File(GmSpaceHostContext.getContext().getCacheDir(), "snapshot_cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, packageName + ".jpg");
    }


    public static GmSpaceResultParcel installApk(Context context, String uri, boolean isIgnorePackageList){
        try {
            boolean isNeedDelAfterInstall = false;
            //GmSpaceUtils.installPackage 接口支持传入apk文件路径和文件夹路径
            //如果是apks和xapk文件，先解压，再安装
            if (uri.endsWith(".apks") || uri.endsWith(".xapk")) {
                isNeedDelAfterInstall = true;

                uri = unZipFile(context,uri);
            }
            //安装前，检查apk是否支持当前系统
            checkApkEnable(context,uri);
            String referrer = getReferrer(context,uri);
            //安装应用，传入apk路径 或者 apks/xapk解压后的文件夹路径
            mGlobalInstallConfig.setIgnorePackageList(false);
            mGlobalInstallConfig.setReferrer(referrer);
            GmSpaceResultParcel GmSpaceResultParcel =  GmSpaceObject.installPackage(uri, mGlobalInstallConfig);
            if(isNeedDelAfterInstall){
                // 拷贝obb文件
                copyObbFile(uri);
                //如果是xapk和apks文件，安装完成后删除解压的文件
                deleteUnZipFile(uri);
            }
            return GmSpaceResultParcel;
        } catch (Exception e) {
            return GmSpaceResultParcel.failure(e);
        }
    }

    private static void copyObbFile(String uri) {
        try {
            String obbFolderPath = uri + File.separator + "Android" + File.separator + "obb";
            File obbFolder = new File(obbFolderPath);
            if (obbFolder.exists() && obbFolder.isDirectory()) {
                FileUtils.copyDirectory(obbFolder, GmSpaceUtils.getHostDir("", GmSpaceUtils.DIRECTORY_KEY_SDCARD_EXTERNAL_OBB));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkApkEnable(Context context,String uri) {
        int minSDKVersion =  getMinSdkVersion(uri,context);
        if (minSDKVersion > Build.VERSION.SDK_INT) {
            // 检查apk要求的最低系统版本
            throw  new RuntimeException("apk最低要求系统api版本 " + minSDKVersion + ", 当前 " + Build.VERSION.SDK_INT);
        }
    }



    private static String unZipFile(Context context,String uri) throws Exception {
        File file = new File(uri);
        String fileName = file.getName();
        //去掉文件后缀名，作为解压的文件夹名称
        String unZipDirName = FilenameUtils.getNameWithoutExtension(fileName);
        //解压路径为 /data/user/0/com.vlite.app/cache/<your_file_name>/
        File unZipFile = new File(context.getCacheDir(), unZipDirName);
        String unZipFilePath = unZipFile.getAbsolutePath();
        //删除残留文件
        deleteUnZipFile(unZipFilePath);
        try {
            //解压文件
            ZipUtil.unpack(new File(uri),unZipFile);
        }catch (Exception e){
            e.printStackTrace();
            //解压失败时，可能有残留文件，删除后，把异常抛出去显示提示信息
            deleteUnZipFile(unZipFilePath);
            throw new Exception(fileName+"解压失败");
        }
        return unZipFilePath;
    }

    private static void deleteUnZipFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }
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
        final Set<String> supportedABIs = getSupportedABIs(apkFilePath);
        if (RuntimeUtils.is64bit()) {
            return supportedABIs.isEmpty() || supportedABIs.contains("arm64-v8a");
        } else {
            return supportedABIs.isEmpty() || supportedABIs.contains("armeabi-v7a") || supportedABIs.contains("armeabi");
        }
    }

    public static String getReferrer(Context context ,String apkFilePath){

        Set<String> supportedABIs = getSupportedABIs(apkFilePath);
        if (RuntimeUtils.is64bit()){
            if (supportedABIs.isEmpty() || supportedABIs.contains("arm64-v8a")){
                return GmSpaceHostContext.getPackageName();
            }else if (supportedABIs.contains("armeabi-v7a") || supportedABIs.contains("armeabi")){
                try {
                    context.getPackageManager().getPackageInfo(PLUGIN_PACKAGE_NAME,0);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException("当前apk为 32位架构，请安装32位插件后重试");
                }
                return PLUGIN_PACKAGE_NAME;
            }
        }else if (supportedABIs.contains("armeabi-v7a") || supportedABIs.contains("armeabi")) {
            return GmSpaceHostContext.getPackageName();
        }
        throw new RuntimeException("apk不支持当前宿主架构 " + ", 当前 " + (RuntimeUtils.is64bit() ? "64位" : "32位"));
    }



    public static Set<String> getSupportedABIs(String apkFilePath){
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
        return supportedABIs;
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


    public static void installApkToHost(Context context, File apkFile) {
        try {
            Uri apkUri;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isHostGranularExternalStoragePermission(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU  && GmSpaceHostContext.getTargetSdkVersion() >= Build.VERSION_CODES.TIRAMISU;
    }

    public static boolean isHostGranularWriteExternalStoragePermission(String permission){
        if (isHostGranularExternalStoragePermission() && Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)){
            return true;
        }
        return false;
    }

}
