package com.vlite.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vlite.app.databinding.ActivityRequestPermissionBinding;
import com.vlite.app.sample.SampleUtils;
import com.vlite.sdk.VLite;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * 虚拟权限管理 申请权限
 */
public class AppRequestPermissionsActivity extends AppCompatActivity {

    public static void start(Context context, String packageName, String appName, String permissionName, String permissionDisplayName) {
        final Intent intent = new Intent(context, AppRequestPermissionsActivity.class);
        intent.putExtra("package_name", packageName);
        intent.putExtra("app_name", appName);
        intent.putExtra("permission_name", permissionName);
        intent.putExtra("permission_display_name", permissionDisplayName);
        intent.putExtra("permissions",new String[]{permissionName});
        context.startActivity(intent);
    }

    private int REQUEST_CODE = 100;

    private ActivityRequestPermissionBinding mBinding;

    private final Queue<String> mRequestPermissionQueue = new LinkedList<>();
    private final Map<String, Integer> mRequestPermissionResults = new LinkedHashMap<>();

    private String mPackageName;
    private String mCurrentRequestPermission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRequestPermissionBinding.inflate(LayoutInflater.from(this));
        setContentView(mBinding.getRoot());

        final Intent intent = getIntent();
        mPackageName = intent.getStringExtra("package_name");
        final String appName = intent.getStringExtra("app_name");
        final String[] permissions = intent.getStringArrayExtra("permissions");
        if (permissions != null && permissions.length > 0){
            for (String permission : permissions) {
                mRequestPermissionQueue.offer(permission);
            }
        }


        mBinding.tvPermissionAllow.setOnClickListener(v -> {
            mRequestPermissionResults.put(mCurrentRequestPermission, PackageManager.PERMISSION_GRANTED);
            showNextRequest(appName);
        });
        mBinding.tvPermissionNotAllow.setOnClickListener(v -> {
            mRequestPermissionResults.put(mCurrentRequestPermission, PackageManager.PERMISSION_DENIED);
            showNextRequest(appName);
        });

        showNextRequest(appName);
    }

    private void showNextRequest(String appName) {
        try {
            if (mRequestPermissionQueue.isEmpty()) {
                // 队列里没有要申请的权限了 直接返回
                final String[] permissions = new String[mRequestPermissionResults.size()];
                final int[] grantResults = new int[mRequestPermissionResults.size()];
                int index = 0;
                for (Map.Entry<String, Integer> entry : mRequestPermissionResults.entrySet()) {
                    permissions[index] = entry.getKey();
                    grantResults[index] = entry.getValue();
                    index++;
                }
                // 设置权限结果
                VLite.get().setPermissionResults(mPackageName, permissions, grantResults);
                // 结果返回给app
                final Intent intent = VLite.get().buildRequestPermissionsResultIntent(permissions, grantResults);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                // 还有权限申请 显示下一个权限
                mCurrentRequestPermission = mRequestPermissionQueue.poll();
                // 宿主有权限 虚拟权限也有 才算有权限
                boolean isGranted = ContextCompat.checkSelfPermission(this, mCurrentRequestPermission) == PackageManager.PERMISSION_GRANTED &&
                        VLite.get().checkPermission(mPackageName, mCurrentRequestPermission) == PackageManager.PERMISSION_GRANTED;
                if (checkSelfPermissionCompat(mCurrentRequestPermission)) {
                    // 宿主有权限 就显示自定义的权限框
                    final PackageManager packageManager = getPackageManager();
                    final PermissionInfo permissionInfo = packageManager.getPermissionInfo(mCurrentRequestPermission, 0);
                    final String permissionDisplayName = permissionInfo.loadLabel(packageManager).toString();
                    mBinding.tvPermissionMessage.setText(String.format("是否允许“%s”%s权限？", appName, permissionDisplayName));
                } else {
                    if (SampleUtils.isHostGranularWriteExternalStoragePermission(mCurrentRequestPermission)){
                        final Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivityForResult(intent,0);
                    }else {
                        // 宿主没权限 直接申请宿主权限
                        ActivityCompat.requestPermissions(this, new String[]{mCurrentRequestPermission}, REQUEST_CODE);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean checkSelfPermissionCompat(String permission){
        if (SampleUtils.isHostGranularWriteExternalStoragePermission(permission)){
            return Environment.isExternalStorageManager();
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

}
