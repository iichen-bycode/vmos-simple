package com.vlite.app.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.vlite.app.R;
import com.vlite.app.activities.AppDetailActivity;
import com.vlite.app.activities.LaunchAppActivity;
import com.vlite.app.adapters.AppItemAdapter;
import com.vlite.app.bean.AppItem;
import com.vlite.app.databinding.FragmentLauncherBinding;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.AssetsUtils;
import com.vlite.app.utils.DialogAsyncTask;
import com.vlite.sdk.VLite;
import com.vlite.sdk.client.VirtualClient;
import com.vlite.sdk.client.virtualservice.pm.VirtualPackageManager;
import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.event.OnReceivedEventListener;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.model.InstallConfig;
import com.vlite.sdk.model.ResultParcel;
import com.vlite.sdk.server.virtualservice.am.LaunchActivityInfo;
import com.vlite.sdk.utils.BitmapUtils;
import com.vlite.sdk.utils.io.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 模拟桌面的样式
 */
@RuntimePermissions
public class LauncherFragment extends Fragment {
    private FragmentLauncherBinding binding;

    private AppItemAdapter adapter;
    private static int icon = 0;
    private static String componentChanged = "";

    private final OnReceivedEventListener receivedEventListener = new OnReceivedEventListener() {
        /**
         * 接收到事件
         * @param type 事件类型
         * @param extras 事件额外信息
         */
        @Override
        public void onReceivedEvent(int type, @NonNull Bundle extras) {
            // 除特别说明的事件外 事件默认回调于子线程
            AppLogger.d("onReceivedEvent -> type = " + BinderEvent.typeToString(type) + ", extras = " + BinderEvent.bundleToString(extras) + ", thread_name = " + Thread.currentThread().getName());
            handleBinderEvent(type, extras);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = FragmentLauncherBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews();

        // 注册事件
        VLite.get().registerReceivedEventListener(receivedEventListener);

        binding.refreshLayout.setOnRefreshListener(() -> {
            loadInstalledApps(view.getContext());
        });

        // 加载已安装的应用
        loadInstalledApps(view.getContext());
    }


    private void getAppLauncherIcon(String packageName) {
        if (TextUtils.isEmpty(packageName)){
            return;
        }
        try {
            componentChanged = packageName;

            ActivityInfo launchActivityInfo = VLite.get().getLaunchActivityInfoForPackage(packageName);
            //刷新界面，重新加载
            List<AppItem> appItems = adapter.getData();
            if (appItems != null && appItems.size() > 0){
                for (AppItem item:appItems){
                    if (item.getPackageName().equals(componentChanged)){
                        String path = item.getIconUri();
                        BitmapUtils.toFile(BitmapUtils.toBitmap(launchActivityInfo.loadIcon(getContext().getPackageManager())), path);
                        item.setIconUri(path);
                        break;
                    }
                }
            }
            adapter.notifyDataSetChanged();

        }catch (Exception e){
            AppLogger.w(e);
        }

    }



    private void bindViews() {
        binding.refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        adapter = new AppItemAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((view, position) -> {
            final AppItem it = adapter.getData().get(position);
            if (it != null) {
                LauncherFragmentPermissionsDispatcher.launchAppWithPermissionCheck(this, it.getPackageName());
            }
        });
        adapter.setOnItemLongClickListener((view, position) -> {
            final AppItem it = adapter.getData().get(position);
            if (it != null) {
                showAppOptionsDialog(it.getPackageName(), it.getAppName());
            }
        });
        Resources res = requireContext().getResources();
        int itemSize = res.getDimensionPixelSize(R.dimen.item_app_size);
        int spanCount = (int) Math.max(4, Math.floor((double) res.getDisplayMetrics().widthPixels / itemSize));
        binding.rvApps.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
        binding.rvApps.setAdapter(adapter);
    }

    /**
     * 事件
     *
     * @param type
     * @param extras
     */
    private void handleBinderEvent(int type, Bundle extras) {
        if (BinderEvent.TYPE_PACKAGE_INSTALLED == type) {
            // 有应用安装
            handlePackageInstalledEvent(extras);
        } else if (BinderEvent.TYPE_PACKAGE_UNINSTALLED == type) {
            // 有应用卸载
            handlePackageUninstalledEvent(extras);
        }else if (BinderEvent.TYPE_COMPONENT_CHANGE_ENABLED == type){
            //更新应用icon
            if (extras != null){
                String pkgName = extras.getString("packageName");
                String clzName = extras.getString("className");
                getAppLauncherIcon(pkgName);
            }

        }
    }

    /**
     * 加载已安装的应用
     *
     * @param context
     */
    @SuppressLint("StaticFieldLeak")
    private void loadInstalledApps(Context context) {
        new AsyncTask<Void, String, List<AppItem>>() {
            private long start = SystemClock.uptimeMillis();

            @Override
            protected void onProgressUpdate(String... values) {
                setSubTitle(values[0]);
            }

            @Override
            protected List<AppItem> doInBackground(Void... voids) {
                publishProgress("正在加载");
                requestInstallPresetApp(packageName -> {
                    publishProgress("正在安装 " + packageName);
                });
                publishProgress("正在加载应用列表");

                start = SystemClock.uptimeMillis();
                final PackageManager pm = context.getPackageManager();
                final List<PackageInfo> packages = VLite.get().getInstalledPackages(0);
                final ArrayList<AppItem> items = new ArrayList<>();
                for (PackageInfo pkg : packages) {
                    final AppItem item = SampleUtils.newAppItem(pm, pkg);
                    if (item != null) items.add(item);
                }
                return items;
            }

            @Override
            protected void onPostExecute(List<AppItem> result) {
                binding.refreshLayout.setRefreshing(false);
                adapter.setData(result);
                adapter.notifyDataSetChanged();

                setSubTitle("已加载" + result.size() + "个应用 " + (SystemClock.uptimeMillis() - start) + "ms");
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * 启动应用
     *
     * @param packageName
     */
    @SuppressLint("StaticFieldLeak")
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void launchApp(String packageName) {
        startActivity(LaunchAppActivity.getIntent(packageName));
    }

    private void showAppOptionsDialog(String packageName, String appName) {
        final Map<String, DialogInterface.OnClickListener> items = new HashMap<>();
        items.put("应用详情", (dialog, which) -> {
            final Intent intent = new Intent(requireContext(), AppDetailActivity.class);
            intent.putExtra("app_name", appName);
            intent.putExtra("package_name", packageName);
            startActivity(intent);
        });
        items.put("卸载", (dialog, which) -> {
            SampleUtils.showUninstallAppDialog(getContext(), appName, (dialog_, which_) -> {
                dialog_.dismiss();
                asyncUninstallApp(getContext(), packageName);
            });
        });
        final String[] labels = items.keySet().toArray(new String[0]);
        final DialogInterface.OnClickListener[] values = items.values().toArray(new DialogInterface.OnClickListener[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle(appName)
                .setItems(labels, (dialog, which) -> {
                    values[which].onClick(dialog, which);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    public static void asyncUninstallApp(Context context, String packageName) {
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
                Toast.makeText(context, result ? "卸载成功" : "卸载失败", Toast.LENGTH_SHORT).show();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 安装的事件
     *
     * @param extras
     */
    @SuppressLint("StaticFieldLeak")
    private void handlePackageInstalledEvent(Bundle extras) {
        final String packageName = extras.getString(BinderEvent.KEY_PACKAGE_NAME);
        final boolean isOverride = extras.getBoolean(BinderEvent.KEY_IS_OVERRIDE);
        if (!isOverride) {
            new AsyncTask<Void, Void, AppItem>() {

                @Override
                protected AppItem doInBackground(Void... voids) {
                    final PackageManager pm = requireContext().getPackageManager();
                    final PackageInfo packageInfo = VLite.get().getPackageInfo(packageName, 0);
                    return SampleUtils.newAppItem(pm, packageInfo);
                }

                @Override
                protected void onPostExecute(AppItem result) {
                    if (result != null) {
                        synchronized (adapter.getData()) {
                            boolean contains = false;
                            for (int i = 0; i < adapter.getData().size(); i++) {
                                final AppItem it = adapter.getData().get(i);
                                if (packageName.equals(it.getPackageName())) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                adapter.getData().add(result);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * 卸载的事件
     *
     * @param extras
     */
    @SuppressLint("StaticFieldLeak")
    private void handlePackageUninstalledEvent(Bundle extras) {
        final String packageName = extras.getString(BinderEvent.KEY_PACKAGE_NAME);
        synchronized (adapter.getData()) {
            int removeIndex = -1;
            for (int i = 0; i < adapter.getData().size(); i++) {
                final AppItem it = adapter.getData().get(i);
                if (packageName.equals(it.getPackageName())) {
                    removeIndex = i;
                    break;
                }
            }
            if (removeIndex >= 0) {
                adapter.getData().remove(removeIndex);
                adapter.notifyDataSetChanged();
            }
        }
    }


    private void setSubTitle(String subTitle) {
        final FragmentActivity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            final ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(subTitle);
            }
        }
    }

    /**
     * 安装预置app
     */
    public void requestInstallPresetApp(OnPreparePresetAppListener listener) {
        try {
            final AssetManager assets = requireContext().getAssets();
            final InputStream stream = assets.open("preset_app_versions.json");
            final JSONObject versionJson = new JSONObject(FileUtils.readStreamToString(stream));
            final Iterator<String> keys = versionJson.keys();
            while (keys.hasNext()) {
                try {
                    final long _start = SystemClock.uptimeMillis();

                    final String packageName = keys.next();
                    // 获取asset的包信息
                    final JSONObject itemJson = versionJson.getJSONObject(packageName);
                    final long newVersionCode = itemJson.optLong("version_code");
                    final String assetPath = itemJson.optString("asset_path");

                    // 如果未安装或者已装的版本较低 才装
                    final PackageInfo packageInfo = VLite.get().getPackageInfo(packageName, 0);
                    if (packageInfo == null || newVersionCode > packageInfo.versionCode) {
                        // 回调给界面
                        if (listener != null) {
                            listener.onPreparePresetApp(packageName);
                        }

                        // 先删除之前的缓存
                        final File packageCache = new File(requireContext().getCacheDir(), assetPath);
                        FileUtils.deleteQuietly(packageCache);
                        // 再复制到缓存
                        AssetsUtils.copyTo(assets, assetPath, packageCache);

                        final ResultParcel result = VLite.get().installPackage(packageCache.getAbsolutePath(), new InstallConfig.Builder().setIgnorePackageList(true).build());
                        if (result.isSucceed()) {
                            AppLogger.i("prepare preset [" + packageName + "] success -> " + packageCache.getAbsolutePath() + " - " + (SystemClock.uptimeMillis() - _start) + "ms");
                        } else {
                            AppLogger.w("prepare preset [" + packageName + "] fail -> " + packageCache.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LauncherFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    private interface OnPreparePresetAppListener {
        void onPreparePresetApp(String packageName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VLite.get().unregisterReceivedEventListener(receivedEventListener);
    }
}
