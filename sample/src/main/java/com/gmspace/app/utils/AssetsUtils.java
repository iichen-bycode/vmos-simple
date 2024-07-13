package com.gmspace.app.utils;

import android.content.res.AssetManager;


import com.gmspace.sdk.proxy.GmSpaceIOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class AssetsUtils {

    public static String[] listNotNull(AssetManager assets, String path) {
        String[] list = null;
        try {
            list = assets.list(path);
        } catch (Exception ignored) {
        }
        return list == null ? new String[0] : list;
    }


    public static void copyTo(AssetManager assets, String path, File output) {
        try {
            final String[] list = assets.list(path);
            // 创建父级文件夹
            final File parentFile = output.getParentFile();
            if (!parentFile.exists()) parentFile.mkdirs();
            // 如果是文件夹
            if (list != null && list.length > 0) {
                for (final String it : list) {
                    final File newFile = new File(output, it);
                    // 创建父级文件夹
                    final File newParentFile = newFile.getParentFile();
                    if (!newParentFile.exists()) newParentFile.mkdirs();
                    copyTo(assets, path + "/" + it, newFile);
                }
            } else {
                // 如果是文件
                InputStream ais = null;
                OutputStream fos = null;
                try {
                    ais = assets.open(path);
                    fos = new FileOutputStream(output);
                    GmSpaceIOUtils.copy(ais, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) fos.close();
                    if (ais != null) ais.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
