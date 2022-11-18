package com.vlite.app.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vlite.app.R;
import com.vlite.app.adapters.InstalledAdapter;
import com.vlite.app.bean.InstalledInfo;
import com.vlite.sdk.logger.AppLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 应用列表dialog
 */
public class InstalledAppDialog extends BaseBottomSheetDialogFragment {
    private ProgressBar mLoadingView;

    private OnClickInstalledItemListener mOnClickInstalledItemListener;

    public InstalledAppDialog setOnClickInstalledItemListener(OnClickInstalledItemListener listener) {
        this.mOnClickInstalledItemListener = listener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_vm_installed, container);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoadingView = view.findViewById(R.id.loading);
        RecyclerView recyclerView = view.findViewById(R.id.rv);

        mLoadingView.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, List<InstalledInfo>>() {
            @Override
            protected List<InstalledInfo> doInBackground(Void... voids) {
                final PackageManager pm = getContext().getPackageManager();
                final List<PackageInfo> packages = pm.getInstalledPackages(0);
                final List<InstalledInfo> infos = new ArrayList<>();
                for (PackageInfo pkg : packages) {
                    try {
                        if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                            final String packageName = pkg.packageName;
                            final File apkFile = new File(pkg.applicationInfo.sourceDir);
                            final String appName = pkg.applicationInfo.loadLabel(pm).toString();
                            final String sourcePath = pkg.splitNames == null ? apkFile.getAbsolutePath() : apkFile.getParent();
                            final InstalledInfo info = new InstalledInfo(packageName, appName, apkFile.length(), sourcePath);
                            final Drawable icon = pkg.applicationInfo.loadIcon(pm);
                            info.setIcon(icon);
                            infos.add(info);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(infos, (o1, o2) -> (int) (o2.getLength() - o1.getLength()));
                return infos;
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(List<InstalledInfo> result) {
                final Context context = getContext();
                if (context != null) {
                    mLoadingView.setVisibility(View.GONE);

                    recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
                    InstalledAdapter installedAdapter = new InstalledAdapter(result);
                    installedAdapter.setOnItemClickListener((v, position) -> {
                        if (mOnClickInstalledItemListener != null) {
                            mOnClickInstalledItemListener.OnClickInstalledItem(installedAdapter.getData().get(position));
                        }
                        dismiss();
                    });
                    installedAdapter.setOnItemLongClickListener((v, position) -> {
                        AppLogger.d(installedAdapter.getData().get(position).getSourcePath());
                    });
                    recyclerView.setAdapter(installedAdapter);
                    mLoadingView.setVisibility(View.GONE);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void show(@NonNull FragmentManager manager) {
        super.show(manager, getClass().getName());
    }

    public interface OnClickInstalledItemListener {
        void OnClickInstalledItem(InstalledInfo item);
    }
}
