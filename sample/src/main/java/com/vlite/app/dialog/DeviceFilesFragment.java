package com.vlite.app.dialog;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.vlite.app.bean.VmFileItem;
import com.vlite.sdk.utils.DatabaseUtils;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 真机文件列表
 */
@RuntimePermissions
public class DeviceFilesFragment extends FilesFragment {

    public static DeviceFilesFragment newInstance(Bundle parent) {
        Bundle args = new Bundle();
        args.putString("title", "本机");
        if (parent != null) {
            args.putAll(parent);
        }
        DeviceFilesFragment fragment = new DeviceFilesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected String getSdcardRealPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    protected void requestExternalFiles(String directoryPath, int queryDirectoryId) {
        DeviceFilesFragmentPermissionsDispatcher.requestExternalFilesFromDeviceWithPermissionCheck(this, directoryPath, queryDirectoryId);
    }

    @Override
    protected JSONArray queryContentProvider(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // 真机
        final Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        JSONArray jsonArray = DatabaseUtils.toJsonArray(cursor);
        cursor.close();
        return jsonArray == null ? new JSONArray() : jsonArray;
    }

    @Override
    protected List<VmFileItem> traverseDirectoryFiles(String parent) {
        try {
            final List<VmFileItem> items = new ArrayList<>();
            final File[] files = new File(parent).listFiles();
            if (files != null) {
                for (File file : files) {
                    final VmFileItem item = createFileItem(file.hashCode(), null, file.getAbsolutePath());
                    if (item != null) items.add(item);
                }
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected File getAbsoluteFile(String path) {
        return new File(path);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void requestExternalFilesFromDevice(String directoryPath, Integer queryDirectoryId) {
        asyncLoadExternalFiles(directoryPath, queryDirectoryId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DeviceFilesFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
