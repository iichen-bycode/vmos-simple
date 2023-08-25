package com.vlite.app.bean;

public class AppPermissionInfo {
    private String permissionDisplayName;
    private String permissionDescription;
    private String permission;
    private int permissionResult;

    public AppPermissionInfo(String permission, int permissionResult) {
        this.permission = permission;
        this.permissionDisplayName = permission;
        this.permissionResult = permissionResult;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public int getPermissionResult() {
        return permissionResult;
    }

    public void setPermissionResult(int permissionResult) {
        this.permissionResult = permissionResult;
    }

    public String getPermissionDisplayName() {
        return permissionDisplayName;
    }

    public void setPermissionDisplayName(String permissionDisplayName) {
        this.permissionDisplayName = permissionDisplayName;
    }

    public String getPermissionDescription() {
        return permissionDescription;
    }

    public void setPermissionDescription(String permissionDescription) {
        this.permissionDescription = permissionDescription;
    }
}
