package com.vlite.app.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samplekit.bean.AppItem;
import com.vlite.app.R;
import com.vlite.app.databinding.ActivityLaunchAppBinding;
import com.vlite.app.utils.GlideUtils;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;

/**
 * 启动app
 */
public class LaunchAppActivity extends AppCompatActivity {

    public static Intent getIntent(AppItem item) {
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(HostContext.getPackageName(), LaunchAppActivity.class.getName()));
        intent.putExtra("item_info", item);
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
        final AppItem itemInfo = intent.getParcelableExtra("item_info");
        if (itemInfo == null) {
            Toast.makeText(this, "参数异常", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        asyncLaunchApp(itemInfo);
    }

    /**
     * 异步启动app
     */
    @SuppressLint("StaticFieldLeak")
    private void asyncLaunchApp(AppItem item) {
        new AsyncTask<Void, Drawable, Void>() {
            @Override
            protected void onProgressUpdate(Drawable... values) {
                // 有windowBackground
                final Drawable windowBackground = (Drawable) values[0];
                applyWindowBackground(windowBackground);
            }

            @Override
            protected void onPreExecute() {
                GlideUtils.loadFadeSkipCache(binding.ivAppIcon, item.getIconUri());
                binding.tvAppName.setText(item.getAppName());
                binding.tvAppPackageName.setText(item.getPackageName());
                binding.tvAppVersion.setText(String.format("%s（%s）", item.getVersionName(), item.getVersionCode()));
            }

            @Override
            protected Void doInBackground(Void... voids) {
                final Drawable drawable = VLite.get().getLaunchActivityWindowBackground(item.getPackageName());
                publishProgress(drawable);

                // 启动app
                VLite.get().launchApplication(item.getPackageName());
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
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
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
