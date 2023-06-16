package com.vlite.app.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.vlite.app.databinding.ActivityLaunchAppBinding;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.GlideUtils;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;

/**
 * 启动app
 */
public class LaunchAppActivity extends AppCompatActivity {

    public static Intent getIntent(String packageName) {
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(HostContext.getPackageName(), LaunchAppActivity.class.getName()));
        intent.putExtra("package_name", packageName);
        return intent;
    }

    private ActivityLaunchAppBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLaunchAppBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        final Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        setFinishOnTouchOutside(false);

        final Intent intent = getIntent();
        final String packageName = intent.getStringExtra("package_name");
        asyncLaunchApp(packageName);
    }

    /**
     * 异步启动app
     */
    @SuppressLint("StaticFieldLeak")
    private void asyncLaunchApp(String packageName) {
        new AsyncTask<Void, Object, Void>() {
            @Override
            protected void onProgressUpdate(Object... values) {
                final String appName = (String) values[0];
                final String iconUri = (String) values[1];
                final PackageInfo packageInfo = (PackageInfo) values[2];

                GlideUtils.loadFadeSkipCache(binding.ivAppIcon, iconUri);
                binding.tvAppName.setText(appName);
                binding.tvAppPackageName.setText(packageInfo.packageName);
                binding.tvAppVersion.setText(String.format("%s（%s）", packageInfo.versionName, packageInfo.versionCode));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                final PackageManager pm = getPackageManager();
                final PackageInfo packageInfo = VLite.get().getPackageInfo(packageName, 0);
                if (packageInfo != null) {
                    final String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                    final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    final String iconUri = SampleUtils.getIconCacheUri(pm, packageInfo.versionCode, applicationInfo,applicationInfo.loadIcon(pm));
                    publishProgress(appName, iconUri, packageInfo);
                }

                // 启动app
                VLite.get().launchApplication(packageName);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                finish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onBackPressed() {

    }
}
