package com.gmspace.app.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gmspace.sdk.GmSpaceInstallConfig;
import com.gmspace.sdk.GmSpaceObject;
import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.gmspace.app.adapters.GoogleInstallAdapter;
import com.gmspace.app.bean.GoogleInstallInfo;
import com.gmspace.app.databinding.DialogGoogleAppInfoBinding;
import com.gmspace.app.download.DownloadHttpClient;
import com.gmspace.app.utils.FileSizeFormat;

import java.io.File;
import java.util.List;

public abstract class InstallKitDialog extends AlertDialog {
    private boolean executing = false;
    protected GoogleInstallAdapter mAdapter;

    protected final DialogGoogleAppInfoBinding mBinding;

    public InstallKitDialog(@NonNull Context context,String title) {
        super(context);
        setCanceledOnTouchOutside(true);

        mBinding = DialogGoogleAppInfoBinding.inflate(LayoutInflater.from(context));
        setView(mBinding.getRoot());
        setTitle(title);

        mBinding.installAllTv.setOnClickListener(v -> {
            installGoogleServiceKit();
        });

        mBinding.uninstallAllTv.setOnClickListener(v -> {
            uninstallGoogleServiceKit();
        });

        loadInstallKitInfos();
    }

    protected abstract List<GoogleInstallInfo> getInstallKitInfos();

    @SuppressLint("StaticFieldLeak")
    private void loadInstallKitInfos(){
        final List<GoogleInstallInfo> list = getInstallKitInfos();
        mAdapter = new GoogleInstallAdapter(list);
        mBinding.googleAppInfoRv.setAdapter(mAdapter);
        mBinding.googleAppInfoRv.setLayoutManager(new LinearLayoutManager(getContext()));

        new AsyncTask<Void, Object, List<GoogleInstallInfo>>() {
            @Override
            protected List<GoogleInstallInfo> doInBackground(Void... voids) {
                for (int i = 0; i < list.size(); i++) {
                    final GoogleInstallInfo it = list.get(i);
                    it.setPackageInfo(getPackageInfo(it.getPackageName()));
                    setInstalled(it);
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<GoogleInstallInfo> result) {
                mAdapter.setData(result);
                mAdapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private File getGoogleApkDirectory() {
        File cacheDir = getContext().getExternalCacheDir();
        File googleApkDir = new File(cacheDir, "googleapk");
        // 确保目录存在
        if (!googleApkDir.exists()) {
            googleApkDir.mkdirs();
        }
        return googleApkDir;
    }

    protected abstract void onPreinstallGoogleServiceKit();

    @SuppressLint("StaticFieldLeak")
    private void installGoogleServiceKit() {
        if (executing) return;
        executing = true;
        new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onProgressUpdate(Integer... values) {
                mAdapter.notifyItemChanged(values[0]);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                onPreinstallGoogleServiceKit();
                final File googleApkDir = getGoogleApkDirectory();
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    final int index = i;
                    GoogleInstallInfo it = mAdapter.getData().get(i);
                    final File apkFile = new File(googleApkDir, new File(it.getDownloadUrl()).getName());
                    if (!apkFile.exists()) {
                        DownloadHttpClient.download(it.getDownloadUrl(), apkFile, (progress, position, contentLength, speedLength) -> {
                            it.setStatusString(String.format("正在下载 %s / %s", FileSizeFormat.formatSize(position), FileSizeFormat.formatSize(contentLength)));
                            publishProgress(index);
                        });
                    }
                    if (apkFile.exists()) {
                        it.setStatusString("正在安装");
                        publishProgress(i);
                        GmSpaceInstallConfig mGlobalInstallConfig = new GmSpaceInstallConfig();
                        mGlobalInstallConfig.setIgnorePackageList(true);
                        GmSpaceObject.installPackage(apkFile.getAbsolutePath(), mGlobalInstallConfig);
                        it.setPackageInfo(getPackageInfo(it.getPackageName()));
                        setInstalled(it);
                    } else {
                        it.setStatusString("下载失败");
                    }
                    publishProgress(i);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                executing = false;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private void uninstallGoogleServiceKit(){
        if (executing) return;
        executing = true;
        new AsyncTask<Void, Integer, List<GoogleInstallInfo>>() {
            @Override
            protected void onProgressUpdate(Integer... values) {
                mAdapter.notifyItemChanged(values[0]);
            }

            @Override
            protected List<GoogleInstallInfo> doInBackground(Void... voids) {
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    GoogleInstallInfo it = mAdapter.getData().get(i);
                    it.setStatusString("正在卸载");
                    publishProgress(i);
                    boolean result = GmSpaceUtils.uninstallPackage(it.getPackageName());
                    if (result) it.setPackageInfo(null);
                    setInstalled(it);
                    publishProgress(i);
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<GoogleInstallInfo> googleInstallInfos) {
                executing = false;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setInstalled(GoogleInstallInfo it){
        it.setStatusString(it.isInstalled() ? it.getVersionName() : "未安装");
    }

    protected abstract PackageInfo getPackageInfo(String packageName);

}
