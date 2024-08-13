package com.gmspace.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gmspace.app.databinding.ActivityAppDetailBinding;
import com.gmspace.sdk.GmSpaceObject;
import com.samplekit.utils.GsonUtils;
import com.gmspace.app.bean.AppDetailInfo;
import com.gmspace.app.bean.AppPermissionInfo;

import java.util.ArrayList;
import java.util.List;

public class AppDetailActivity extends AppCompatActivity {
    private ActivityAppDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppDetailBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        final Bundle extras = getIntent().getExtras();
        final String packageName = extras.getString("package_name");
        final Intent intent = new Intent(this, AppPermissionActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
        finish();//跳到App权限页面了，当前空白页面finish
    }

    @SuppressLint("StaticFieldLeak")
    private void loadAppDetail(String packageName) {
        new AsyncTask<Void, Void, AppDetailInfo>() {

            @Override
            protected AppDetailInfo doInBackground(Void... voids) {
                final PackageInfo info = GmSpaceObject.getGmSpacePackageInfo(packageName);
                final AppDetailInfo result = new AppDetailInfo(info.packageName);
                final List<AppPermissionInfo> permissionInfos = new ArrayList<>();
                final String[] permissions = GmSpaceObject.getDangerousPermissions(info.packageName);
                final int[] permissionResults = GmSpaceObject.checkPermissions(info.packageName, permissions);
                for (int i = 0; i < permissions.length; i++) {
                    permissionInfos.add(new AppPermissionInfo(permissions[i], permissionResults[i]));
                }
                result.setPermissions(permissionInfos);
                return result;
            }

            @Override
            protected void onPostExecute(AppDetailInfo result) {
                super.onPostExecute(result);
                bindAppDetailViews(result);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void bindAppDetailViews(AppDetailInfo result) {
        Log.d("----------------", GsonUtils.toPrettyJson(result));
    }
}
