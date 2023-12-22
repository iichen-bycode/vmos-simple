package com.vlite.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
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
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.samplekit.dialog.DeviceFileSelectorDialog;
import com.samplekit.dialog.DeviceInstalledAppDialog;
import com.vlite.app.adapters.FloatMenuAdapter;
import com.vlite.app.adapters.ProcessItemAdapter;
import com.vlite.app.bean.FloatMenuItem;
import com.vlite.app.bean.ProcessInfo;
import com.vlite.app.bean.RunningInfo;
import com.vlite.app.databinding.ActivityMainBinding;
import com.vlite.app.databinding.DialogInputLocationBinding;
import com.vlite.app.databinding.DialogProcessListBinding;
import com.vlite.app.databinding.LayoutNavigationHeaderBinding;
import com.vlite.app.databinding.LayoutWindowMenuBinding;
import com.vlite.app.dialog.GoogleAppInfoDialog;
import com.vlite.app.dialog.MicroGInstallDialog;
import com.vlite.app.dialog.VmInstalledAppDialog;
import com.vlite.app.fragments.LauncherFragment;
import com.vlite.app.fragments.RunningTaskFragment;
import com.vlite.app.sample.SampleActivityCallbackDelegate;
import com.vlite.app.sample.SampleAppManager;
import com.vlite.app.sample.SampleApplicationLifecycleDelegate;
import com.vlite.app.sample.SampleDeviceUtils;
import com.vlite.app.sample.SampleIntentInterceptor;
import com.vlite.app.sample.SampleLocationStore;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.DialogAsyncTask;
import com.vlite.app.utils.FileSizeFormat;
import com.vlite.app.utils.RuntimeUtils;
import com.vlite.app.view.FloatPointView;
import com.vlite.sdk.LiteConfig;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.ServiceContext;
import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.event.OnReceivedEventListener;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.model.ConfigurationContext;
import com.vlite.sdk.model.DeviceEnvInfo;
import com.vlite.sdk.model.PackageConfiguration;
import com.vlite.sdk.model.ResultParcel;
import com.vlite.sdk.utils.BitmapUtils;
import com.vlite.sdk.utils.io.FileUtils;
import com.vlite.sdk.utils.io.FilenameUtils;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private GoogleAppInfoDialog googleAppInfoDialog;
    private MicroGInstallDialog microGInfoDialog;
    private DeviceFileSelectorDialog deviceFileSelectorDialog;

    static {
        System.loadLibrary("mytestproject");
    }

    private static native void hookAndTestOpenat(long bhookPtr);

    private final OnReceivedEventListener receivedEventListener = new OnReceivedEventListener() {
        /**
         * 接收到事件
         * @param type 事件类型
         * @param extras 事件额外信息
         */
        @Override
        public void onReceivedEvent(int type, @NonNull Bundle extras) {
            // 除特别说明的事件外 事件默认回调于子线程
            AppLogger.d("onReceivedEvent -> type = " + BinderEvent.typeToString(type) + ", thread_name = " + Thread.currentThread().getName() + ", extras = " + SampleUtils.eventToPrintString(extras));

            handleReceivedEvent(type, extras);
            final Fragment fragment = getSupportFragmentManager().findFragmentById(binding.contentFragment.getId());
            if (fragment instanceof LauncherFragment) {
                ((LauncherFragment) fragment).handleBinderEvent(type, extras);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        bindViews();

        // 注册事件
        VLite.get().registerReceivedEventListener(receivedEventListener);

        applyConfiguration();

        asyncApplyVirtualDeviceInfo();

        // bhook测试代码
//        hookAndTestOpenat(Native.getBhookApi());
    }

    private void bindViews() {
        try {
            final Toolbar toolbar = binding.toolbar;
            setSupportActionBar(toolbar);
            final String title = String.format("%s %s (%d) %s", getString(R.string.app_name), com.vlite.app.BuildConfig.VERSION_NAME, com.vlite.app.BuildConfig.VERSION_CODE, com.vlite.app.BuildConfig.BUILD_TYPE);
            setTitle(title);
            final Class<? extends View> cls = toolbar.getClass();
            final Method getTitleTextViewMethod = cls.getDeclaredMethod("getTitleTextView");
            getTitleTextViewMethod.setAccessible(true);
            final TextView titleTextView = (TextView) getTitleTextViewMethod.invoke(toolbar);
            if (titleTextView.getPaint().measureText(title) / getResources().getDisplayMetrics().widthPixels > 0.75f) {
                toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleStyleSmall);
                toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubTitleStyleSmall);
            } else {
                toolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubTitleStyleNormal);
            }
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            toolbar.setPadding(toolbar.getPaddingLeft(), toolbar.getPaddingTop() + statusBarHeight, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final LayoutNavigationHeaderBinding headerBinding = LayoutNavigationHeaderBinding.inflate(LayoutInflater.from(this));
        binding.navigationView.addHeaderView(headerBinding.getRoot());
        headerBinding.tvAppName.setText(String.format("SDK版本 %s (%d)", VLite.get().getSDKVersionName(), VLite.get().getSDKVersionCode()));
        headerBinding.getRoot().setOnClickListener(v -> {
            final StringBuilder sb = new StringBuilder("SDK版本：").append(com.vlite.sdk.BuildConfig.VERSION_NAME).append(" (")
                    .append(com.vlite.sdk.BuildConfig.VERSION_CODE).append(")\n")
                    .append("示例版本：").append(com.vlite.app.BuildConfig.VERSION_NAME).append(" (")
                    .append(com.vlite.app.BuildConfig.VERSION_CODE).append(")\n")
                    .append(VLite.get().getSDKIdentifier());
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
        // 华为手机用不了google服务 让华为才用虚拟设备信息
        if (!Build.BRAND.equalsIgnoreCase("huawei")){
            return;
        }
        new AsyncTask<Void, Object, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // 默认虚拟一个设备信息
                DeviceEnvInfo deviceInfo = SampleDeviceUtils.getVirtualDeviceInfo();
                if (deviceInfo == null) {
                    deviceInfo = DeviceEnvInfo.random();

//                    String brand = "OPPO";
//                    String model = "PDRM00";
//                    String product = "PDRM00";
//                    String device = "OP4EA7";
//                    String fingerprint = "OPPO/PDRM00/OP4EA7:12/SKQ1.210216.001/R.202201162315:user/release-keys";
                    String brand = "google";
                    String model = "Pixel 4 XL";
                    String product = "coral";
                    String device = "coral";
                    String fingerprint = "google/coral/coral:13/TP1A.221005.002.B2/9382335:user/release-keys";

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

    @SuppressLint("StaticFieldLeak")
    private void applyConfiguration() {
        new AsyncTask<Void, Object, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SampleLocationStore.clear();
                final File localTmp = new File(getExternalCacheDir(), "/data/local/tmp");
                if (!localTmp.exists()) localTmp.mkdirs();
                VLite.get().setConfigurationContext(new ConfigurationContext.Builder()
                        .setPackageBlacklist(new HashSet<>(Arrays.asList("com.android.vending", "com.google.android.gsf", "com.google.android.gms")))
                        .setUseInternalSdcard(false)
                        // 启用进程预热示例 最多预热2个进程
                        .setMaxPreheatProcessCount(2)
                        // 自定义io重定向规则示例
                        // 重定向文件 要重定向的文件/重定向的目标路径/是否白名单
                        // 注释掉重定向功能，导致微信语音失效，目前发现/proc/cpuinfo 没有在对应的重定向目录找到对应的文件，手动补上对应的文件后微信语音正常
                        //.addFileRedirectRule("/proc/cpuinfo", new File(getExternalCacheDir(), "/proc/cpuinfo").getAbsolutePath(), false)
                        // 重定向目录 要重定向的目录/重定向的目标路径/是否白名单
                        //.addDirectoryRedirectRule("/data/local/tmp", localTmp.getAbsolutePath(), false)
                        .build());
                VLite.get().setPackageConfiguration(new PackageConfiguration.Builder()
                        .setEnableTraceAnr(true)
                        .setEnableTraceNativeCrash(true)
                        .setApplicationLifecycleDelegate(SampleApplicationLifecycleDelegate.class)
                        .setActivityCallbackDelegate(SampleActivityCallbackDelegate.class)
                        .setIntentInterceptor(SampleIntentInterceptor.class)
                        .build());
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
            case R.id.menu_float_menu:
                // 悬浮菜单
                showFloatMenu();
                break;
            // 应用管理
            case R.id.menu_vm_install_app:
                // 安装应用
                showChooseApkFragment();
                break;
            case R.id.menu_google_app_install:
                if (googleAppInfoDialog == null) {
                    googleAppInfoDialog = new GoogleAppInfoDialog(this);
                }
                googleAppInfoDialog.show();
                break;
            case R.id.menu_microg_install:
                if (microGInfoDialog == null) {
                    microGInfoDialog = new MicroGInstallDialog(this);
                }
                microGInfoDialog.show();
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
            // 杀死全部进程
            case R.id.menu_kill_all:
                showKillAppDialog();
                break;
            // 触摸事件
            case R.id.menu_touch:
                showSendMotionEventDialog();
                break;
            case R.id.menu_vm_location:
                showInputLocationDialog();
                break;
            // 发送广播
            case R.id.menu_send_broadcast:
                sendBroadcastToApp();
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
        if (deviceFileSelectorDialog == null) {
            deviceFileSelectorDialog = DeviceFileSelectorDialog.newInstance("请选择Apk", new String[]{".apk", ".apks", ".xapk"}, true);
            deviceFileSelectorDialog.setOnFileSelectorListener(item -> {
                // 安装apk
                asyncInstallApkFile(new File(item.getAbsolutePath()));
            });
        }
        deviceFileSelectorDialog.show(getSupportFragmentManager());
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
//        Context context = dialog!=null&&dialog.isVisible()?dialog.getContext():MainActivity.this;
        Context context = MainActivity.this;
        new DialogAsyncTask<Void, Void, Boolean>(context) {
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
            setSubtitle("文件不存在 " + (src == null ? null : src.getAbsolutePath()));
            return;
        }
        new DialogAsyncTask<String, String, ResultParcel>(this) {

            @Override
            protected void onPreExecute() {
                super.showProgressDialog("正在安装");
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.updateProgressDialog(values[0]);
            }

            @Override
            protected ResultParcel doInBackground(String... uris) {
                String uri = uris[0];
                return SampleUtils.installApk(MainActivity.this,uri,false);
            }
            @Override
            protected void onPostExecute(ResultParcel result) {
                super.onPostExecute(result);
                if (result.isSucceed()) {
                    setSubtitle("应用安装成功 " + (result.getData() == null ? "" : result.getData().getString(BinderEvent.KEY_PACKAGE_NAME)));
                } else {
                    setSubtitle("应用安装失败 " + result.getMessage());
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, src.getAbsolutePath());
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
                final Debug.MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
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
                    final Drawable drawable = SampleUtils.convertLauncherDrawable(info.loadIcon(pm));
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

    private void showInputLocationDialog() {
        if (!LiteConfig.get().getCustomServiceClassNames().containsKey(ServiceContext.LOCATION_SERVICE)) {
            Toast.makeText(this, "此示例需要解开registerCustomService注释", Toast.LENGTH_SHORT).show();
            return;
        }
        final DialogInputLocationBinding dialogBinding = DialogInputLocationBinding.inflate(LayoutInflater.from(this));
        Location lastFakeLocation = SampleLocationStore.getFakeLocation();
        if (lastFakeLocation != null){
            dialogBinding.etLongitude.setText(String.valueOf(lastFakeLocation.getLongitude()).replaceAll("\\.?0*$", ""));
            dialogBinding.edLatitude.setText(String.valueOf(lastFakeLocation.getLatitude()).replaceAll("\\.?0*$", ""));
        }

        new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setTitle("手动输入经纬度")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    double longitude = Double.parseDouble(dialogBinding.etLongitude.getText().toString());
                    double latitude = Double.parseDouble(dialogBinding.edLatitude.getText().toString());
                    final Location fakeLocation = new Location(LocationManager.GPS_PROVIDER);
                    fakeLocation.setLongitude(longitude);
                    fakeLocation.setLatitude(latitude);
                    SampleLocationStore.setFakeLocation(fakeLocation);
                    Toast.makeText(this, "设置位置信息 " + longitude + ", " + latitude, Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void sendBroadcastToApp() {
        final Intent intent = new Intent("sample.register.test");
        VLite.get().sendBroadcast(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void showKillAppDialog() {
        new DialogAsyncTask<Void, Void, Void>(this) {

            @Override
            protected void onPreExecute() {
                super.showProgressDialog(null);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                final List<String> packageNames = VLite.get().getRunningPackageNames();
                for (String packageName : packageNames) {
                    VLite.get().forceStopPackage(packageName);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                Toast.makeText(MainActivity.this, "已清理运行中的应用", Toast.LENGTH_SHORT).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void showSendMotionEventDialog() {
        FloatPointView.show(this);
    }

    private void handleReceivedEvent(int type, Bundle extras) {
        if (BinderEvent.TYPE_ACTIVITY_LIFECYCLE == type) {
            final String packageName = extras.getString(BinderEvent.KEY_BASE_INFO_PACKAGE_NAME);
            final String methodName = extras.getString(BinderEvent.KEY_METHOD_NAME);
            SampleAppManager.onActivityLifecycle(packageName, methodName);
        }
    }

    private void showFloatMenu() {
        int offsetY = (int) (getResources().getDisplayMetrics().heightPixels * 0.2f);
        EasyFloat.with(this)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.LEFT)
                .setTag("menu")
                .setDragEnable(true)
                .setGravity(Gravity.START | Gravity.TOP, 0, offsetY)
                .setLayout(R.layout.layout_window_menu, view -> {
                    handleInflateWindowView(view.findViewById(R.id.layout_window_content));
                })
                .show();
    }

    private void handleInflateWindowView(View view) {
        final LayoutWindowMenuBinding menuBinding = LayoutWindowMenuBinding.bind(view);
        menuBinding.layoutClickMenu.setOnClickListener(v -> {
            menuBinding.layoutMenuContent.setVisibility(View.VISIBLE);
            menuBinding.layoutClickMenu.setVisibility(View.GONE);
        });
        menuBinding.ivClose.setOnClickListener(v -> {
            menuBinding.layoutClickMenu.setVisibility(View.VISIBLE);
            menuBinding.layoutMenuContent.setVisibility(View.GONE);
        });
        final List<FloatMenuItem> items = new ArrayList<>();
        items.add(new FloatMenuItem(R.drawable.ic_landscape, "强制横屏", v -> {
            sendForceOrientationCommand(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, "force_landscape");
        }));
        items.add(new FloatMenuItem(R.drawable.ic_portrait, "强制竖屏", v -> {
            sendForceOrientationCommand(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, "force_portrait");
        }));
        /*items.add(new FloatMenuItem(R.mipmap.ic_launcher, "画中画", v -> {
            final Intent intent = new Intent("command_" + "com.vlite.unittest");
            intent.putExtra("command_id", "force_pip");
            VLite.get().sendBinderBroadcast(intent);
            final ConfigurationContext configurationContext = VLite.get().getConfigurationContext();
            final ConfigurationContext newConfigurationContext = configurationContext.newBuilder()
                    .setForcePictureInPicture(true)
                    .build();
            VLite.get().setConfigurationContext(newConfigurationContext);
        }));*/
        menuBinding.rvMenuList.setLayoutManager(new GridLayoutManager(view.getContext(), Math.min(4, items.size())));
        final FloatMenuAdapter floatMenuAdapter = new FloatMenuAdapter(items);
        floatMenuAdapter.setOnItemClickListener((itemView, position) -> {
            items.get(position).getClickListener().onClick(itemView);
        });
        menuBinding.rvMenuList.setAdapter(floatMenuAdapter);
    }

    private void sendForceOrientationCommand(int orientation, String command_id) {
        final ConfigurationContext configurationContext = VLite.get().getConfigurationContext();
        if (orientation != configurationContext.getForceOrientation()){
            final ConfigurationContext newConfigurationContext = configurationContext.newBuilder()
                    .setForceOrientation(orientation)
                    .build();
            VLite.get().setConfigurationContext(newConfigurationContext);
        }
        final String foregroundPackageName = SampleAppManager.getForegroundPackageName();
        if (!TextUtils.isEmpty(foregroundPackageName)) {
            final Intent intent = new Intent("command_" + foregroundPackageName);
            intent.putExtra("command_id", command_id);
            VLite.get().sendBinderBroadcast(intent);
        }
    }

    private void setSubtitle(CharSequence subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        VLite.get().unregisterReceivedEventListener(receivedEventListener);
    }

}