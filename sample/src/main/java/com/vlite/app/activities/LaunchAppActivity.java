package com.vlite.app.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.vlite.app.R;
import com.vlite.app.databinding.ActivityLaunchAppBinding;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.GlideUtils;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.utils.BitmapUtils;

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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        binding = ActivityLaunchAppBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
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
                if (values.length >= 4) {
                    // 有windowBackground
                    final Drawable windowBackground = (Drawable) values[3];
                    applyWindowBackground(windowBackground);
                } else {
                    final String appName = (String) values[0];
                    final String iconUri = (String) values[1];
                    final PackageInfo packageInfo = (PackageInfo) values[2];

                    GlideUtils.loadFadeSkipCache(binding.ivAppIcon, iconUri);
                    binding.tvAppName.setText(appName);
                    binding.tvAppPackageName.setText(packageInfo.packageName);
                    binding.tvAppVersion.setText(String.format("%s（%s）", packageInfo.versionName, packageInfo.versionCode));
                }
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
                final Drawable drawable = VLite.get().getLaunchActivityWindowBackground(packageName);
                publishProgress(null, null, null, drawable);

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

    private void applyWindowBackground(Drawable windowBackground) {
        if (windowBackground == null || windowBackground instanceof ColorDrawable) {
            binding.getRoot().setVisibility(View.VISIBLE);
        } else {
            final LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{new ColorDrawable(Color.WHITE), windowBackground});
            getWindow().setBackgroundDrawable(layerDrawable);
            binding.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
