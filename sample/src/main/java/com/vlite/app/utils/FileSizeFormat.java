package com.vlite.app.utils;

import java.text.DecimalFormat;

public class FileSizeFormat {

    public static String formatMB(long bytes) {
        if (bytes <= 0) return "0 MB";
        int digitGroups = 2;
        return new DecimalFormat("0.0").format(bytes / Math.pow(1024.0, (double) digitGroups)).toString() + " MB";
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) bytes) / Math.log10(1024.0));
        return new DecimalFormat("###0.#").format(bytes / Math.pow(1024.0, digitGroups)) + " " + units[digitGroups];
    }

}
