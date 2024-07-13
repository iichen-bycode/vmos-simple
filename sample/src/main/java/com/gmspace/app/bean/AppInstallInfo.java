package com.gmspace.app.bean;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

import java.util.List;

public class AppInstallInfo {
    private PackageInfo installed;
    private Drawable logo;
    private String appName;
    private int versionCode;
    private String versionName;
    private long size;
    private List<AppPermissionInfo> permissions;

    public PackageInfo getInstalled() {
        return installed;
    }

    public void setInstalled(PackageInfo installed) {
        this.installed = installed;
    }

    public Drawable getLogo() {
        return logo;
    }

    public void setLogo(Drawable logo) {
        this.logo = logo;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public List<AppPermissionInfo> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<AppPermissionInfo> permissions) {
        this.permissions = permissions;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
