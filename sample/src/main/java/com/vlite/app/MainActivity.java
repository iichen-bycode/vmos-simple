package com.vlite.app;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.vlite.app.databinding.ActivityMainBinding;
import com.vlite.app.databinding.LayoutNavigationHeaderBinding;
import com.vlite.app.dialog.DeviceFileSelectorFragment;
import com.vlite.app.dialog.InstalledAppDialog;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.DialogAsyncTask;
import com.vlite.sdk.VLite;
import com.vlite.sdk.model.DeviceEnvInfo;
import com.vlite.sdk.model.ResultParcel;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        bindViews();

        asyncApplyVirtualDeviceInfo();
    }

    private void bindViews() {
        try {
            final String title = String.format("%s %s (%d) %s", getString(R.string.app_name), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.BUILD_TYPE);
            setTitle(title);
            final View toolbar = findViewById(androidx.appcompat.R.id.action_bar);
            if (toolbar instanceof Toolbar) {
                final Class<? extends View> cls = toolbar.getClass();
                final Method getTitleTextViewMethod = cls.getDeclaredMethod("getTitleTextView");
                getTitleTextViewMethod.setAccessible(true);
                final TextView titleTextView = (TextView) getTitleTextViewMethod.invoke(toolbar);
                if (titleTextView.getPaint().measureText(title) / getResources().getDisplayMetrics().widthPixels > 0.75f) {
                    ((Toolbar) toolbar).setTitleTextAppearance(this, R.style.ToolbarTitleStyleSmall);
                    ((Toolbar) toolbar).setSubtitleTextAppearance(this, R.style.ToolbarSubTitleStyleSmall);
                } else {
                    ((Toolbar) toolbar).setSubtitleTextAppearance(this, R.style.ToolbarSubTitleStyleNormal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final LayoutNavigationHeaderBinding headerBinding = LayoutNavigationHeaderBinding.inflate(LayoutInflater.from(this));
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        headerBinding.tvAppName.setText(String.format("SDK版本 %s (%d)", VLite.get().getSDKVersionName(), VLite.get().getSDKVersionCode()));
        headerBinding.getRoot().setOnClickListener(v -> {
            final StringBuilder sb = new StringBuilder("SDK版本：").append(com.vlite.sdk.BuildConfig.VERSION_NAME).append(" (")
                    .append(com.vlite.sdk.BuildConfig.VERSION_CODE).append(")\n")
                    .append("示例版本：").append(BuildConfig.VERSION_NAME).append(" (")
                    .append(BuildConfig.VERSION_CODE).append(")");
            new AlertDialog.Builder(v.getContext())
                    .setTitle("版本信息")
                    .setMessage(sb.toString())
                    .setNegativeButton("关闭", null)
                    .show();
        });
        binding.navigationView.setNavigationItemSelectedListener(item -> onOptionsItemSelected(item));
    }

    @SuppressLint("StaticFieldLeak")
    private void asyncApplyVirtualDeviceInfo() {
        new AsyncTask<Void, Object, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // 默认虚拟一个设备信息
                DeviceEnvInfo deviceInfo = SampleUtils.getVirtualDeviceInfo();
                if (deviceInfo == null) {
                    deviceInfo = DeviceEnvInfo.random();

                    String brand = "HUAWEI";
                    String model = "VOG-TL00";
                    String product = "VOG-TL00";
                    String device = "HWVOG";
                    String fingerprint = "HUAWEI/VOG-TL00/HWVOG:10/HUAWEIVOG-TL00/10.1.0.162C01:user/release-keys";

                    deviceInfo.putBuildField("BRAND", brand);
                    deviceInfo.putBuildField("MANUFACTURER", brand);
                    deviceInfo.putBuildField("MODEL", model);
                    deviceInfo.putBuildField("PRODUCT", product);
                    deviceInfo.putBuildField("DEVICE", device);
                    deviceInfo.putBuildField("FINGERPRINT", fingerprint);
//                    if (GmsUtils.GMS_DEBUG) {
//                        deviceInfo.putBuildField("FINGERPRINT", "robolectric");
//                    }

                    deviceInfo.putSystemProperty("ro.product.brand", brand);
                    deviceInfo.putSystemProperty("ro.product.manufacturer", brand);
                    deviceInfo.putSystemProperty("ro.product.model", model);
                    deviceInfo.putSystemProperty("ro.product.name", product);
                    deviceInfo.putSystemProperty("ro.product.device", device);
                    deviceInfo.putSystemProperty("ro.build.fingerprint", fingerprint);

                    SampleUtils.putVirtualDeviceInfo(deviceInfo);
                }
                VLite.get().setDeviceEnvInfo(deviceInfo);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        int groupId = item.getGroupId();
        switch (itemId) {
            case R.id.menu:
                toggleDrawer();
                return true;
            // 应用管理
            case R.id.menu_vm_install_app:
                // 安装应用
                showChooseApkFragment();
                break;
            case R.id.menu_vm_install_app_from_device:
                // 导入真机应用
                showDeviceInstalledAppDialog();
                break;
            case R.id.menu_vm_running_app:
                // 运行中的应用
                showRunningAppDialog();
                break;
            // 虚拟设备信息
            case R.id.menu_vm_device:
                // 虚拟设备信息
                showVirtualDeviceDialog();
                break;
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    /**
     * 选择apk
     */
    public void showChooseApkFragment() {
        DeviceFileSelectorFragment fragment = DeviceFileSelectorFragment.newInstance("请选择Apk", new String[]{".apk"});
        fragment.setOnFileSelectorListener(item -> {
            // 安装apk
            asyncInstallApkFile(new File(item.getAbsolutePath()));
        });
        fragment.show(getSupportFragmentManager());
    }


    /**
     * 真机已安装的应用列表
     */
    private void showDeviceInstalledAppDialog() {
        new InstalledAppDialog()
                .setOnClickInstalledItemListener(item -> {
                    asyncInstallApkFile(new File(item.getSourcePath()));
                })
                .show(getSupportFragmentManager());
    }

    /**
     * 异步安装apk
     *
     * @param src
     */
    @SuppressLint("StaticFieldLeak")
    public void asyncInstallApkFile(File src) {
        if (src == null || !src.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        new DialogAsyncTask<Void, String, ResultParcel>(this) {

            @Override
            protected void onPreExecute() {
                super.showProgressDialog("正在安装");
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.updateProgressDialog(values[0]);
            }

            @Override
            protected ResultParcel doInBackground(Void... voids) {
                return VLite.get().installPackage(src.getAbsolutePath());
            }

            @Override
            protected void onPostExecute(ResultParcel result) {
                super.onPostExecute(result);
                if (result.isSucceed()) {
                    setSubtitle("应用安装成功");
                } else {
                    setSubtitle("应用安装失败 " + result.getMessage());
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    private void showRunningAppDialog() {
        final List<String> packageNames = VLite.get().getRunningPackageNames();
        new AlertDialog.Builder(this)
                .setTitle("正在运行的应用")
                .setItems(packageNames.toArray(new String[0]), (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    /**
     * 虚拟设备信息
     */
    private void showVirtualDeviceDialog() {
        final DeviceEnvInfo deviceInfo = SampleUtils.getVirtualDeviceInfo();
        new AlertDialog.Builder(this)
                .setTitle("虚拟设备信息")
                .setNegativeButton("关闭", null)
                .setPositiveButton("应用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VLite.get().setDeviceEnvInfo(deviceInfo);
                    }
                })
                .show();

    }


    private void setSubtitle(CharSequence subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

}