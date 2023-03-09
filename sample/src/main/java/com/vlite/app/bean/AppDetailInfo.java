package com.vlite.app.bean;

import java.util.List;

public class AppDetailInfo {
    private String packageName;
    private String appName;
    private List<AppPermissionInfo> permissions;

    public AppDetailInfo(String packageName) {
        this.packageName = packageName;
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

    public List<AppPermissionInfo> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<AppPermissionInfo> permissions) {
        this.permissions = permissions;
    }
}
