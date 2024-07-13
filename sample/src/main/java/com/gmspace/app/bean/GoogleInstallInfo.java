package com.gmspace.app.bean;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

public class GoogleInstallInfo {

    private String statusString;
    private String appName;
    private String packageName;
    private String downloadUrl;
    private String versionName;
    private int versionCode;

    public GoogleInstallInfo(String packageName, String appName, String downloadUrl) {
        this.packageName = packageName;
        this.appName = appName;
        this.downloadUrl = downloadUrl;
    }

    public void setPackageInfo(PackageInfo pkgInfo) {
        if (pkgInfo != null) {
            this.versionName = pkgInfo.versionName;
            this.versionCode = pkgInfo.versionCode;
        }else{
            this.versionName = null;
            this.versionCode = 0;
        }
        this.setStatusString(null);
    }

    public boolean isInstalled() {
        return !TextUtils.isEmpty(this.versionName);
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }
}
