package com.vlite.app.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vlite.app.R;
import com.vlite.app.adapters.GoogleInstallAdapter;
import com.vlite.app.bean.GoogleInstallInfo;
import com.vlite.app.download.DownloadHttpClient;
import com.vlite.app.utils.FileSizeFormat;
import com.vlite.sdk.VLite;
import com.vlite.sdk.model.ResultParcel;
import com.vlite.sdk.utils.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleAppInfoDialog extends AlertDialog {
    private static final String TAG = "GoogleAppInfoDialog";
    private boolean downloading = false;
    private final GoogleInstallAdapter adapter;
    private final List<GoogleInstallInfo> googleInstallInfoList = new ArrayList<>();
    // 创建一个固定大小的线程池，最多同时运行3个线程
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    private final Handler handler = new Handler(Looper.getMainLooper());

    public GoogleAppInfoDialog(@NonNull Context context) {
        super(context);
        setCanceledOnTouchOutside(true);

        View view = getLayoutInflater().inflate(R.layout.dialog_google_app_info, null);
        setView(view);
        setTitle("谷歌三件套信息");

        adapter = new GoogleInstallAdapter(googleInstallInfoList);
        RecyclerView googleAppInfoRv = view.findViewById(R.id.google_app_info_rv);
        googleAppInfoRv.setAdapter(adapter);
        googleAppInfoRv.setLayoutManager(new LinearLayoutManager(getContext()));

        initData();

        view.findViewById(R.id.install_all_tv).setOnClickListener(v -> {
            downloadApk();
        });

        view.findViewById(R.id.uninstall_all_tv).setOnClickListener(v -> {
            for (int i = 0; i < adapter.getData().size(); i++) {
                GoogleInstallInfo installInfo = adapter.getData().get(i);
                boolean result = VLite.get().uninstallPackage(installInfo.getPackageName());
                if (result) {
                    installInfo.setPackageInfo(null);
                    adapter.notifyItemChanged(i);
                }
            }
        });

        view.findViewById(R.id.delete_apk_file_tv).setOnClickListener(v -> {
            if (FileUtils.deleteQuietly(getGoogleApkDirectory())) {
                Toast.makeText(getContext(),"删除成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        executorService.execute(() -> {
            //此下载地址仅用于DEMO测试用，请勿在生产环境使用
            GoogleInstallInfo gp = new GoogleInstallInfo("Google Play商店", "com.android.vending",
                    "https://files.vmos.pro/sample/gpstore.apk");
            gp.setPackageInfo(VLite.get().getPackageInfo(gp.getPackageName(), 0));
            googleInstallInfoList.add(gp);

            GoogleInstallInfo gms = new GoogleInstallInfo("Google Play服务", "com.google.android.gms",
                    "https://files.vmos.pro/sample/gpservice.apk");
            gms.setPackageInfo(VLite.get().getPackageInfo(gms.getPackageName(), 0));
            googleInstallInfoList.add(gms);

            GoogleInstallInfo gsf = new GoogleInstallInfo("Google Services Framework", "com.google.android.gsf",
                    "https://files.vmos.pro/sample/gsf.apk");
            gsf.setPackageInfo(VLite.get().getPackageInfo(gsf.getPackageName(), 0));
            googleInstallInfoList.add(gsf);
            handler.post(() -> {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private File getGoogleApkDirectory() {
        File cacheDir = getContext().getCacheDir();
        File googleApkDir = new File(cacheDir, "googleapk");
        // 确保目录存在
        if (!googleApkDir.exists()) {
            googleApkDir.mkdirs();
        }
        return googleApkDir;
    }

    private void downloadApk() {
        if (downloading) return;
        executorService.execute(() -> {
            downloading = true;
            for (GoogleInstallInfo installInfo : googleInstallInfoList) {
                File googleApkDir = getGoogleApkDirectory();
                File apkFile = new File(googleApkDir, new File(installInfo.getDownloadUrl()).getName());
                Log.d(TAG, "GoogleAppInfoDialog: " + apkFile.getAbsolutePath());
                boolean result;
                if (apkFile.exists()) {
                    //如果文件已存在，并且app已安装这个时候忽略
                    if (installInfo.isInstalled()) {
                        continue;
                    }
                    result = true;
                } else {
                    result = DownloadHttpClient.download(installInfo.getDownloadUrl(), apkFile, (progress, position, contentLength, speedLength) -> {
                        handler.post(() -> {
                            if (installInfo != null) {
                                installInfo.setDescribe(String.format("正在下载 %s / %s", FileSizeFormat.formatSize(position), FileSizeFormat.formatSize(contentLength)));
                            }
                            if (adapter != null) {
                                adapter.notifyItemChanged(installInfo.getPosition());
                            }
                        });
                    });
                }

                ResultParcel resultParcel;
                if (result) {
                    resultParcel = VLite.get().installPackage(apkFile.getAbsolutePath());
                    if (resultParcel.isSucceed()) {
                        installInfo.setPackageInfo(VLite.get().getPackageInfo(installInfo.getPackageName(), 0));
                    }
                } else {
                    resultParcel = null;
                }
                handler.post(() -> {
                    if (installInfo != null) {
                        installInfo.setDescribe(null);
                    }

                    if (resultParcel != null && !resultParcel.isSucceed()) {
                        Toast.makeText(getContext(), "应用安装失败 " + resultParcel.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if (adapter != null) {
                        adapter.notifyItemChanged(installInfo.getPosition());
                    }
                });
            }
            downloading = false;
        });
    }

    @Override
    public void show() {
        super.show();
        refreshData();
    }

    private void refreshData() {
        executorService.execute(() -> {
            for (GoogleInstallInfo googleInstallInfo : googleInstallInfoList) {
                googleInstallInfo.setPackageInfo(VLite.get().getPackageInfo(googleInstallInfo.getPackageName(), 0));
            }
            handler.post(() -> {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }
}
