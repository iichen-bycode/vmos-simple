package com.gmspace.app.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
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

import com.gmspace.sdk.GmSpaceEvent;
import com.gmspace.sdk.GmSpaceResultParcel;
import com.gmspace.sdk.proxy.GmSpaceFileUtils;
import com.gmspace.sdk.proxy.GmSpaceHostContext;
import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.samplekit.bean.AppItem;
import com.gmspace.app.R;
import com.gmspace.app.activities.AppDetailActivity;
import com.gmspace.app.activities.LaunchAppActivity;
import com.gmspace.app.adapters.AppItemAdapter;
import com.gmspace.app.databinding.FragmentLauncherBinding;
import com.gmspace.app.sample.SampleUtils;
import com.gmspace.app.utils.AssetsUtils;
import com.gmspace.app.utils.DialogAsyncTask;

import org.json.JSONObject;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 模拟桌面的样式
 */
//@RuntimePermissions
public class LauncherFragment extends Fragment {
    private FragmentLauncherBinding binding;

    private AppItemAdapter adapter;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        binding.refreshLayout.setOnRefreshListener(() -> {
            loadInstalledApps(view.getContext());
        });

        // 加载已安装的应用
        loadInstalledApps(view.getContext());
    }

    private void handleComponentSettingChangeEvent(Bundle extras) {
        String packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME);
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        int index = -1;
        final List<AppItem> data = adapter.getData();
        for (int i = 0; i < data.size(); i++) {
            final AppItem item = data.get(i);
            if (TextUtils.equals(packageName, item.getPackageName())) {
                final PackageInfo pkg = GmSpaceUtils.getPackageInfo(packageName, 0);
                final AppItem newItem = SampleUtils.newAppItem(GmSpaceHostContext.getContext().getPackageManager(), pkg);
                item.setIconUri(newItem.getIconUri());
                index = i;
                break;
            }
        }
        notifyItemChanged(index);
    }

    private void bindViews() {
        binding.refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        adapter = new AppItemAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((view, position) -> {
            final AppItem it = adapter.getData().get(position);
            if (it != null) {
                launchApp(it);
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
    public void handleBinderEvent(int type, Bundle extras) {
        if (GmSpaceEvent.TYPE_PACKAGE_INSTALLED == type) {
            // 有应用安装
            handlePackageInstalledEvent(extras);
        } else if (GmSpaceEvent.TYPE_PACKAGE_UNINSTALLED == type) {
            // 有应用卸载
            handlePackageUninstalledEvent(extras);
        }else if (GmSpaceEvent.TYPE_COMPONENT_SETTING_CHANGE == type){
            //组件状态变化
            handleComponentSettingChangeEvent(extras);

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
                final List<PackageInfo> packages = GmSpaceUtils.getInstalledPackages(0);
                final ArrayList<AppItem> items = new ArrayList<>();
                for (PackageInfo pkg : packages) {
                    final AppItem item = SampleUtils.newAppItem(pm, pkg);
                    if (item != null){
                        items.add(item);
                    }
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
     */
    @SuppressLint("StaticFieldLeak")
//    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void launchApp(AppItem item) {
        startActivity(LaunchAppActivity.getIntent(item));
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
                return GmSpaceUtils.uninstallPackage(packageName);
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
        final String packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME);
        final boolean isOverride = extras.getBoolean(GmSpaceEvent.KEY_IS_OVERRIDE);
        if (!isOverride) {
            new AsyncTask<Void, Void, AppItem>() {

                @Override
                protected AppItem doInBackground(Void... voids) {
                    final PackageManager pm = requireContext().getPackageManager();
                    final PackageInfo packageInfo = GmSpaceUtils.getPackageInfo(packageName, 0);
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
        final String packageName = extras.getString(GmSpaceEvent.KEY_PACKAGE_NAME);
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

    private void notifyItemChanged(int index){
        final Runnable runnable = () -> {
            if (index >= 0) {
                adapter.notifyItemChanged(index);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
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
            final JSONObject versionJson = new JSONObject(GmSpaceFileUtils.readStreamToString(stream));
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
                    final PackageInfo packageInfo = GmSpaceUtils.getPackageInfo(packageName, 0);
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
                        final GmSpaceResultParcel result = SampleUtils.installApk(getContext(),packageCache.getAbsolutePath(),true);
                        if (result.isSucceed()) {
                            Log.d("iichen","prepare preset [" + packageName + "] success -> " + packageCache.getAbsolutePath() + " - " + (SystemClock.uptimeMillis() - _start) + "ms");
                        } else {
                            Log.d("iichen","prepare preset [" + packageName + "] fail -> " + packageCache.getAbsolutePath());
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
//        LauncherFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    private interface OnPreparePresetAppListener {
        void onPreparePresetApp(String packageName);
    }

}
