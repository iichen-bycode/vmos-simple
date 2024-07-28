package com.gmspace.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.multidex.BuildConfig;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gmspace.app.bean.AppItemEnhance;
import com.gmspace.sdk.GmSpaceConfigContextBuilder;
import com.gmspace.sdk.GmSpaceEvent;
import com.gmspace.sdk.GmSpaceObject;
import com.gmspace.sdk.GmSpacePackageBuilder;
import com.gmspace.sdk.GmSpacePackageConfiguration;
import com.gmspace.sdk.GmSpaceResultParcel;
import com.gmspace.sdk.OnGmSpaceReceivedEventListener;
import com.gmspace.sdk.proxy.GmSpaceBitmapUtils;
import com.gmspace.sdk.proxy.GmSpaceHostContext;
import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;

import com.samplekit.bean.AppItem;
import com.samplekit.bean.InstalledInfo;
import com.samplekit.dialog.DeviceFileSelectorDialog;
import com.samplekit.dialog.DeviceInstalledAppDialog;
import com.samplekit.utils.GsonUtils;
import com.gmspace.app.adapters.FloatMenuAdapter;
import com.gmspace.app.adapters.ProcessItemAdapter;
import com.gmspace.app.bean.FloatMenuItem;
import com.gmspace.app.bean.ProcessInfo;
import com.gmspace.app.bean.RunningInfo;
import com.gmspace.app.databinding.ActivityMainBinding;
import com.gmspace.app.databinding.DialogProcessListBinding;
import com.gmspace.app.databinding.LayoutNavigationHeaderBinding;
import com.gmspace.app.databinding.LayoutWindowMenuBinding;
import com.gmspace.app.dialog.GoogleAppInfoDialog;
import com.gmspace.app.dialog.MicroGInstallDialog;
import com.gmspace.app.dialog.VmInstalledAppDialog;
import com.gmspace.app.fragments.LauncherFragment;
import com.gmspace.app.fragments.RunningTaskFragment;
import com.gmspace.app.sample.SampleAppManager;
import com.gmspace.app.sample.SampleUtils;
import com.gmspace.app.service.AppKeepAliveService;
import com.gmspace.app.utils.DialogAsyncTask;
import com.gmspace.app.utils.FileSizeFormat;
import com.gmspace.app.view.FloatPointView;
import com.tencent.mmkv.MMKV;
import com.vlite.sdk.context.HostContext;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private GoogleAppInfoDialog googleAppInfoDialog;
    private MicroGInstallDialog microGInfoDialog;
    private DeviceFileSelectorDialog deviceFileSelectorDialog;
    private VmInstalledAppDialog vmInstalledAppDialog;

    private final OnGmSpaceReceivedEventListener receivedEventListener = new OnGmSpaceReceivedEventListener() {
        /**
         * 接收到事件
         * @param type 事件类型
         * @param extras 事件额外信息
         */
        @Override
        public void onReceivedEvent(int type, @NonNull Bundle extras) {
            // 除特别说明的事件外 事件默认回调于子线程
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
        GmSpaceObject.registerGmSpaceReceivedEventListener(receivedEventListener);

        applyConfiguration();

        //用于解决服务进程意外死亡
        Intent intent = new Intent(this, AppKeepAliveService.class);
        startService(intent);
    }

    private void bindViews() {
        try {
            final Toolbar toolbar = binding.toolbar;
            setSupportActionBar(toolbar);
            setSubtitle(BuildConfig.APPLICATION_ID);
            final String title = String.format("%s %s (%d) %s", getString(R.string.app_name), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.BUILD_TYPE);
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
            titleTextView.setShadowLayer(1f, 1, 1, Color.GRAY);
            final Method getSubTitleTextViewMethod = cls.getDeclaredMethod("getSubtitleTextView");
            getSubTitleTextViewMethod.setAccessible(true);
            final TextView subTitleTextView = (TextView) getSubTitleTextViewMethod.invoke(toolbar);
            if (subTitleTextView != null) subTitleTextView.setShadowLayer(1f, 1, 1, Color.GRAY);

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
        headerBinding.tvAppName.setText("GmSpace");
        binding.navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected);
    }

    @SuppressLint("StaticFieldLeak")
    private void applyConfiguration() {
        new AsyncTask<Void, Object, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final File localTmp = new File(getExternalCacheDir(), "/data/local/tmp");
                if (!localTmp.exists()) localTmp.mkdirs();
                GmSpaceConfigContextBuilder builder = new GmSpaceConfigContextBuilder();
                builder.setGmSpaceForcePictureInPicture(true);
                builder.setGmSpaceUseInternalSdcard(true);
                builder.setGmSpaceIsolatedHost(true);
                builder.setGmSpaceKeepPackageSessionCache(true);
                GmSpaceObject.setGmSpaceConfigurationContext(builder);

                GmSpacePackageBuilder gmSpacePackageBuilder = new GmSpacePackageBuilder();
                gmSpacePackageBuilder.setGmSpaceEnableTraceAnr(true);
                gmSpacePackageBuilder.setGmSpaceAllowCreateShortcut(true);
                gmSpacePackageBuilder.setGmSpaceAllowCreateDynamicShortcut(true);
                gmSpacePackageBuilder.setGmSpaceEnableTraceNativeCrash(true);
                GmSpaceObject.setGmSpacePackageConfiguration(gmSpacePackageBuilder);
                Log.d("iichen", ">>>>>>>接入setGmSpacePackageConfiguration");
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
//            case R.id.menu_float_menu:
//                // 悬浮菜单
//                showFloatMenu();
//                break;
            // 应用管理
            case R.id.menu_vm_install_app:
                // 安装应用
                showChooseApkFragment();
                break;
//            case R.id.menu_google_app_install:
//                if (googleAppInfoDialog == null) {
//                    googleAppInfoDialog = new GoogleAppInfoDialog(this);
//                }
//                googleAppInfoDialog.show();
//                break;
//            case R.id.menu_microg_install:
//                if (microGInfoDialog == null) {
//                    microGInfoDialog = new MicroGInstallDialog(this);
//                }
//                microGInfoDialog.show();
//                break;
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
//            case R.id.menu_touch:
//                showSendMotionEventDialog();
//                break;
            // 发送广播
            case R.id.menu_send_broadcast:
                sendBroadcastToApp();
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
        if (vmInstalledAppDialog == null) {
            vmInstalledAppDialog = new VmInstalledAppDialog("已安装的应用");
        }
        vmInstalledAppDialog.setOnClickInstalledItemListener((item, position) -> {
            showInstalledAppMenuDialog(item, position);
        });
        vmInstalledAppDialog.show(getSupportFragmentManager());
    }

    private void showInstalledAppMenuDialog(InstalledInfo item, final int position) {
        final Map<String, DialogInterface.OnClickListener> menus = new HashMap<>();
        menus.put("卸载应用", (dialog, which) -> {
            dialog.dismiss();
            SampleUtils.showUninstallAppDialog(this, item.getAppName(), (dialog_, which_) -> {
                asyncUninstallAppAndRemoveItem(item.getPackageName(), position);
            });
        });
        menus.put("查看信息", (dialog, which) -> {
            dialog.dismiss();
            final AlertDialog infoDialog = new AlertDialog.Builder(this)
                    .setTitle(item.getAppName())
                    .setMessage(GsonUtils.toPrettyJson(item))
                    .show();
            final TextView tv = infoDialog.findViewById(android.R.id.message);
            tv.setTextIsSelectable(true);
        });
        menus.put("安装到真机", (dialog, which) -> {
            dialog.dismiss();
            SampleUtils.installApkToHost(this, new File(item.getSourcePath()));
        });
        final String[] labels = menus.keySet().toArray(new String[0]);
        final DialogInterface.OnClickListener[] listeners = menus.values().toArray(new DialogInterface.OnClickListener[0]);
        new AlertDialog.Builder(this)
                .setTitle(item.getAppName())
                .setItems(labels, (dialog, which) -> listeners[which].onClick(dialog, which))
                .setNegativeButton("关闭", null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private void asyncUninstallAppAndRemoveItem(String packageName, int position) {
//        Context context = dialog!=null&&dialog.isVisible()?dialog.getContext():MainActivity.this;
        Context context = MainActivity.this;
        new DialogAsyncTask<Void, Void, Boolean>(context) {
            @Override
            protected void onPreExecute() {
                super.showProgressDialog("正在卸载");
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return GmSpaceUtils.uninstallPackage(packageName);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (vmInstalledAppDialog != null) {
                    vmInstalledAppDialog.removeItem(position);
                }
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
        new DialogAsyncTask<String, String, GmSpaceResultParcel>(this) {

            @Override
            protected void onPreExecute() {
                super.showProgressDialog("正在安装");
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.updateProgressDialog(values[0]);
            }

            @Override
            protected GmSpaceResultParcel doInBackground(String... uris) {
                String uri = uris[0];
                return SampleUtils.installApk(MainActivity.this, uri, false);
            }

            @Override
            protected void onPostExecute(GmSpaceResultParcel result) {
                super.onPostExecute(result);
                if (result.isSucceed()) {
                    setSubtitle("应用安装成功 " + (result.getData() == null ? "" : result.getData().getString(GmSpaceEvent.KEY_PACKAGE_NAME)));
                } else {
                    Log.d("iichen", ">>>>>>>>>>>>>>>>>>>>>>安装失败 " + result.getMessage());
                    test(src.getAbsolutePath());
                    setSubtitle("应用安装失败 " + result.getMessage());
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, src.getAbsolutePath());
    }

    private void test(String absolutePath) {
        if(isAppInstalled("com.gmspace.sdk")) {
            Intent intent = new Intent();
            intent.putExtra("mPath", absolutePath);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("com.gmspace.sdk", "com.gmspace.ext.PluginInstallActivity"));
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivityForResult(intent, 0x0001);
        } else {
            Toast.makeText(this,"请先安装32位插件",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x0001 && resultCode == 0x0002 && data != null) {
            String json = data.getStringExtra("mAppItem");
            Uri uri = data.getData();
            AppItemEnhance mAppItem = GsonUtils.toObject(json,AppItemEnhance.class);
            if (mAppItem != null && uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    File outputFile = new File(GmSpaceHostContext.getContext().getCacheDir()+ File.separator + "icon_cache_v2",mAppItem.getAppName()+".png");
                    if (inputStream != null) {
                        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            long totalBytes = 0;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                                totalBytes += length;
                            }
                            outputStream.flush();
                            Log.d("iichen","File copied, total bytes: " + totalBytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("iichen","Error writing file: " + e.getMessage());
                        }
                    } else {
                        Log.d("iichen","Input stream is null");
                    }
                    mAppItem.setIconUri(outputFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final Fragment fragment = getSupportFragmentManager().findFragmentById(binding.contentFragment.getId());
                if (fragment instanceof LauncherFragment) {
                    ((LauncherFragment) fragment).notify32AppInstall(mAppItem);
                }
            }
        }
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
                final List<ActivityManager.RunningAppProcessInfo> processes = GmSpaceUtils.getRunningAppProcesses();
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
                final List<String> runningPackageNames = GmSpaceUtils.getRunningPackageNames();
                final PackageManager pm = getPackageManager();
                for (String packageName : runningPackageNames) {
                    final RunningInfo item = new RunningInfo();

                    final ApplicationInfo info = GmSpaceUtils.getApplicationInfo(packageName, 0);
                    final Drawable drawable = SampleUtils.convertLauncherDrawable(info.loadIcon(pm));
                    final Bitmap bitmap = GmSpaceBitmapUtils.toBitmap(drawable);
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

    private void sendBroadcastToApp() {
        final Intent intent = new Intent("sample.register.test");
        GmSpaceUtils.sendBroadcast(intent);
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
                final List<String> packageNames = GmSpaceUtils.getRunningPackageNames();
                for (String packageName : packageNames) {
                    GmSpaceUtils.forceStopPackage(packageName);
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
        if (GmSpaceEvent.TYPE_ACTIVITY_LIFECYCLE == type) {
            final String packageName = extras.getString(GmSpaceEvent.KEY_BASE_INFO_PACKAGE_NAME);
            final String methodName = extras.getString(GmSpaceEvent.KEY_METHOD_NAME);
            final String className = extras.getString(GmSpaceEvent.KEY_CLASS_NAME);
            SampleAppManager.onActivityLifecycle(packageName, methodName, className);

        } else if (type == GmSpaceEvent.TYPE_APPLICATION_CREATE) {
            GmSpacePackageConfiguration configuration = GmSpaceObject.getPackageConfiguration();
            Log.d("iichen", ">>>>>>>>handleReceivedEvent getGmSpaceApplicationLifecycleDelegateClassName " + configuration.getGmSpaceApplicationLifecycleDelegateClassName());
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

        }));
        items.add(new FloatMenuItem(R.drawable.ic_portrait, "强制竖屏", v -> {

        }));
        items.add(new FloatMenuItem(R.drawable.ic_pip, "画中画", v -> {

        }));
        menuBinding.rvMenuList.setLayoutManager(new GridLayoutManager(view.getContext(), Math.min(4, items.size())));
        final FloatMenuAdapter floatMenuAdapter = new FloatMenuAdapter(items);
        floatMenuAdapter.setOnItemClickListener((itemView, position) -> {
            items.get(position).getClickListener().onClick(itemView);
        });
        menuBinding.rvMenuList.setAdapter(floatMenuAdapter);
    }

    private void setSubtitle(CharSequence subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        GmSpaceObject.unregisterGmSpaceReceivedEventListener(receivedEventListener);
    }

}