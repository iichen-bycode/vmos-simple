package com.vlite.app.bean;

import android.content.pm.PackageInfo;


import java.util.Objects;

public class GoogleInstallInfo {
    private String name;
    private String packageName;
    private String downloadUrl;
    private String describe;
    private int position;
    private PackageInfo packageInfo = null;

    public GoogleInstallInfo(String name, String packageName, String downloadUrl) {
        this.name = name;
        this.packageName = packageName;
        this.downloadUrl = downloadUrl;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getDescribe() {
        return describe != null ? describe : (isInstalled()
                ? packageInfo.versionName : "未安装");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isInstalled() {
        return packageInfo != null;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoogleInstallInfo that = (GoogleInstallInfo) o;
        return getPackageName().equals(that.getPackageName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }
}
