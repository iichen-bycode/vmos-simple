package com.vlite.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.multidex.BuildConfig;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vlite.app.adapters.ProcessItemAdapter;
import com.vlite.app.bean.ProcessInfo;
import com.vlite.app.bean.RunningInfo;
import com.vlite.app.databinding.ActivityMainBinding;
import com.vlite.app.databinding.DialogProcessListBinding;
import com.vlite.app.databinding.LayoutNavigationHeaderBinding;
import com.vlite.app.dialog.DeviceFileSelectorFragment;
import com.vlite.app.dialog.DeviceInstalledAppDialog;
import com.vlite.app.dialog.VmInstalledAppDialog;
import com.vlite.app.fragments.RunningTaskFragment;
import com.vlite.app.sample.SampleApplicationLifecycleDelegate;
import com.vlite.app.sample.SampleDeviceUtils;
import com.vlite.app.sample.SampleIntentInterceptor;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.DialogAsyncTask;
import com.vlite.app.utils.FileSizeFormat;
import com.vlite.app.view.FloatPointView;
import com.vlite.sdk.VLite;
import com.vlite.sdk.model.DeviceEnvInfo;
import com.vlite.sdk.model.PackageConfiguration;
import com.vlite.sdk.model.ResultParcel;
import com.vlite.sdk.utils.BitmapUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        bindViews();

        applyConfiguration();

        asyncApplyVirtualDeviceInfo();
    }

    private void bindViews() {
        try {
            final String title = String.format("%s %s (%d) %s", getString(R.string.app_name), com.vlite.app.BuildConfig.VERSION_NAME, com.vlite.app.BuildConfig.VERSION_CODE, com.vlite.app.BuildConfig.BUILD_TYPE);
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
                DeviceEnvInfo deviceInfo = SampleDeviceUtils.getVirtualDeviceInfo();
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

                    SampleDeviceUtils.putVirtualDeviceInfo(deviceInfo);
                }
                VLite.get().setDeviceEnvInfo(deviceInfo);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void applyConfiguration() {
        VLite.get().setPackageConfiguration(new PackageConfiguration.Builder()
                .setApplicationLifecycleDelegate(SampleApplicationLifecycleDelegate.class)
                .setIntentInterceptor(SampleIntentInterceptor.class)
                .build());
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
            case R.id.menu_installed_app:
                // 已安装的应用
                showInstalledAppDialog();
                break;
            case R.id.menu_vm_running_app:
                // 运行中的应用
                showRunningTasks();
                break;
            // 运行中的进程
            case R.id.menu_vm_running_process:
                showRunningProcessesDialog();
                break;
            // 触摸事件
            case R.id.menu_touch:
                showSendMotionEventDialog();
                break;
            // 虚拟设备信息
            case R.id.menu_vm_device:
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
        DeviceFileSelectorFragment fragment = DeviceFileSelectorFragment.newInstance("请选择Apk", new String[]{".apk"}, true);
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
        new DeviceInstalledAppDialog("导入真机应用")
                .setOnClickInstalledItemListener((item, position) -> {
                    asyncInstallApkFile(new File(item.getSourcePath()));
                })
                .show(getSupportFragmentManager());
    }

    /**
     * 虚拟机已安装的应用列表
     */
    @SuppressLint("StaticFieldLeak")
    private void showInstalledAppDialog() {
        VmInstalledAppDialog dialog = new VmInstalledAppDialog("已安装的应用");
        dialog.setOnClickInstalledItemListener((item, position) -> {
            SampleUtils.showUninstallAppDialog(this, item.getAppName(), (dialog_, which_) -> {
                asyncUninstallAppAndRemoveItem(dialog, item.getPackageName(), position);
            });
        });
        dialog.show(getSupportFragmentManager());
    }

    @SuppressLint("StaticFieldLeak")
    private void asyncUninstallAppAndRemoveItem(VmInstalledAppDialog dialog, String packageName, int position) {
        new DialogAsyncTask<Void, Void, Boolean>(dialog.getContext()) {
            @Override
            protected void onPreExecute() {
                super.showProgressDialog("正在卸载");
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return VLite.get().uninstallPackage(packageName);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                dialog.removeItem(position);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    /**
     * 虚拟设备信息
     */
    private void showVirtualDeviceDialog() {
        final DeviceEnvInfo deviceInfo = SampleDeviceUtils.getVirtualDeviceInfo();
        new AlertDialog.Builder(this)
                .setTitle("虚拟设备信息")
                .setNegativeButton("关闭", null)
                .setPositiveButton("应用", (dialog, which) -> {
                    VLite.get().setDeviceEnvInfo(deviceInfo);
                })
                .show();

    }


    /**
     * 运行中的进程信息
     */
    @SuppressLint("StaticFieldLeak")
    private void showRunningProcessesDialog() {
        final Context context = this;
        // 获取进程列表
        new DialogAsyncTask<Void, Void, List<ProcessInfo>>(context) {

            @Override
            protected void onPreExecute() {
                super.showProgressDialog(null);
            }

            @Override
            protected List<ProcessInfo> doInBackground(Void... voids) {
                final List<ActivityManager.RunningAppProcessInfo> processes = VLite.get().getRunningAppProcesses();
                final int[] pids = new int[processes.size()];
                for (int i = 0; i < processes.size(); i++) {
                    pids[i] = processes.get(i).pid;
                }
                final List<ProcessInfo> infos = new ArrayList<>();
                final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                final Debug.MemoryInfo[] memoryInfos =  am.getProcessMemoryInfo(pids);
                for (int i = 0; i < processes.size(); i++) {
                    final Debug.MemoryInfo memoryInfo = memoryInfos[i];
                    final ActivityManager.RunningAppProcessInfo process = processes.get(i);
                    final ProcessInfo processInfo = new ProcessInfo();
                    processInfo.uid = process.uid;
                    processInfo.pid = process.pid;
                    processInfo.processName = process.processName;
                    processInfo.pss = memoryInfo.getTotalPss() * 1024L;
                    infos.add(processInfo);
                }
                return infos;
            }

            @Override
            protected void onPostExecute(List<ProcessInfo> infos) {
                super.onPostExecute(infos);
                final DialogProcessListBinding dialogBinding = DialogProcessListBinding.inflate(LayoutInflater.from(context));
                long sumPss = 0;
                for (ProcessInfo info : infos) sumPss += info.pss;

                dialogBinding.tvVmProcSumMem.setText(FileSizeFormat.formatSize(sumPss));
                final ProcessItemAdapter processAdapter = new ProcessItemAdapter(infos);
                dialogBinding.rvProcessList.setAdapter(processAdapter);
                dialogBinding.rvProcessList.setLayoutManager(new LinearLayoutManager(context));
                new AlertDialog.Builder(context)
                        .setTitle("运行中的进程")
                        .setView(dialogBinding.getRoot())
                        .setNegativeButton("关闭", null)
                        .show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    private void showRunningTasks() {
        new DialogAsyncTask<Void, Void, List<RunningInfo>>(this) {

            @Override
            protected void onPreExecute() {
                super.showProgressDialog(null);
            }

            @Override
            protected List<RunningInfo> doInBackground(Void... voids) {
                final List<RunningInfo> items = new ArrayList<>();
                final List<String> runningPackageNames = VLite.get().getRunningPackageNames();
                final PackageManager pm = getPackageManager();
                for (String packageName : runningPackageNames) {
                    final RunningInfo item = new RunningInfo();

                    final ApplicationInfo info = VLite.get().getApplicationInfo(packageName, 0);
                    final Drawable drawable = info.loadIcon(pm);
                    final Bitmap bitmap = BitmapUtils.toBitmap(drawable);
                    if (bitmap != null) {
                        final Palette palette = Palette.from(bitmap).generate();
                        item.setBackgroundColor(palette.getLightVibrantColor(Color.WHITE));
                    }

                    item.setIcon(drawable);
                    item.setSnapshot(SampleUtils.getSnapshotCacheFile(packageName).getAbsolutePath());

                    item.setAppName(info.loadLabel(pm).toString());
                    item.setPackageName(packageName);
                    items.add(item);
                }
                return items;
            }

            @Override
            protected void onPostExecute(List<RunningInfo> result) {
                super.onPostExecute(result);
                if (result.isEmpty()) {
                    Toast.makeText(MainActivity.this, "没有正在运行的应用", Toast.LENGTH_SHORT).show();
                } else {
                    new RunningTaskFragment()
                            .setRunningInfos(result)
                            .show(getSupportFragmentManager());
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showSendMotionEventDialog(){
        FloatPointView.show(this);
    }

    private void setSubtitle(CharSequence subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

}