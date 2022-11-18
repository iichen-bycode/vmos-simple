package com.vlite.app.bean;

import android.graphics.drawable.Drawable;

public class InstalledInfo {
    private String packageName;
    private String appName;
    private long length;
    private String sourcePath;
    private Drawable icon;

    public InstalledInfo() {
    }

    public InstalledInfo(String packageName, String appName, long length, String sourcePath) {
        this.packageName = packageName;
        this.appName = appName;
        this.length = length;
        this.sourcePath = sourcePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
