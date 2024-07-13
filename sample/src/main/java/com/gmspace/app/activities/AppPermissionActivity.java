package com.gmspace.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gmspace.app.databinding.ActivityAppPermissionBinding;
import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.gmspace.app.adapters.PermissionAdapter;
import com.gmspace.app.bean.AppPermissionInfo;


import java.util.ArrayList;
import java.util.List;

public class AppPermissionActivity extends AppCompatActivity {
    private ActivityAppPermissionBinding binding;

    private String mAppName;
    private String mPackageName;

    private final int REQUEST_CODE = 100;

    private PermissionAdapter mPermissionAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppPermissionBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        final Intent intent = getIntent();
        mAppName = intent.getStringExtra("app_name");
        mPackageName = intent.getStringExtra("package_name");
        setTitle(mAppName + " - 权限");
        loadAppPermissions(mPackageName);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadAppPermissions(String packageName) {
        new AsyncTask<Void, Void, List<AppPermissionInfo>>() {

            @Override
            protected List<AppPermissionInfo> doInBackground(Void... voids) {
                final PackageManager pm = getPackageManager();
                final List<AppPermissionInfo> appPermissionInfos = new ArrayList<>();
                final String[] permissions = GmSpaceUtils.getDangerousPermissions(packageName);
                final int[] permissionResults = GmSpaceUtils.checkPermissions(packageName, permissions);
                for (int i = 0; i < permissions.length; i++) {
                    final String permission = permissions[i];
                    final AppPermissionInfo appPermissionInfo = new AppPermissionInfo(permission, permissionResults[i]);
                    try {
                        final PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
                        appPermissionInfo.setPermissionDisplayName(permissionInfo.loadLabel(pm).toString());
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                    appPermissionInfos.add(appPermissionInfo);
                }
                return appPermissionInfos;
            }

            @Override
            protected void onPostExecute(List<AppPermissionInfo> result) {
                super.onPostExecute(result);
                bindDataForViews(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void bindDataForViews(List<AppPermissionInfo> result) {
        binding.rvPermissionList.setLayoutManager(new LinearLayoutManager(this));
        mPermissionAdapter = new PermissionAdapter(result);
        mPermissionAdapter.setOnItemClickListener((view, position) -> {
            final AppPermissionInfo item = mPermissionAdapter.getData().get(position);
            if (PackageManager.PERMISSION_GRANTED == item.getPermissionResult()) {
                // 关闭权限
                GmSpaceUtils.setPermissionResult(mPackageName, item.getPermission(), PackageManager.PERMISSION_DENIED);

                item.setPermissionResult(PackageManager.PERMISSION_DENIED);
                mPermissionAdapter.notifyItemChanged(position, item.getPermissionResult());
            } else {
                // 开启权限
//                ActivityCompat.requestPermissions(AppPermissionActivity.this, new String[]{item.getPermission()}, REQUEST_CODE);
                AppRequestPermissionsActivity.start(this, mPackageName, mAppName, item.getPermission(), item.getPermissionDisplayName());
            }
        });
        binding.rvPermissionList.setAdapter(mPermissionAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            // 宿主有权限 虚拟应用才有权限 更新虚拟应用权限状态
            GmSpaceUtils.setPermissionResults(mPackageName, permissions, grantResults);

            for (int i = 0; i < mPermissionAdapter.getData().size(); i++) {
                final AppPermissionInfo item = mPermissionAdapter.getData().get(i);
                if (item.getPermission().equals(permissions[0])) {
                    item.setPermissionResult(grantResults[0]);
                    mPermissionAdapter.notifyItemChanged(i, item.getPermissionResult());
                    break;
                }
            }
        }
    }
}
