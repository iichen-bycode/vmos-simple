package com.vlite.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vlite.app.R;
import com.vlite.app.adapters.InstallerPermissionAdapter;
import com.vlite.app.bean.AppInstallInfo;
import com.vlite.app.bean.AppPermissionInfo;
import com.vlite.app.databinding.ActivityAppInstallerBinding;
import com.vlite.app.sample.SampleUtils;
import com.vlite.app.utils.FileSizeFormat;
import com.vlite.app.utils.UriUtils;
import com.vlite.app.view.ProgressButton;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.model.ResultParcel;
import com.vlite.sdk.utils.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class AppInstallerActivity extends AppCompatActivity {
    private final int REQUEST_CODE_PERMISSION = 0;

    private ActivityAppInstallerBinding binding;
    private String origin;

    private Uri requestIntentData;
    private File requestInstallFile;
    private ResultParcel installResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppInstallerBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        setTitle(getString(R.string.title_app_installer, getString(R.string.app_name)));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding.tvAppVersionValue.setSelected(true);
        binding.tvAppVersionValue.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), binding.tvAppVersionValue.getText().toString(), Toast.LENGTH_SHORT).show();
        });
//        binding.btnInstallCancel.setOnClickListener(v -> onBackPressed());
        binding.btnInstallConfirm.setOnClickListener(v -> {
            if (installResult == null) {
                requestInstallPackage();
            } else {
                finishAndRemoveTask();
            }
        });
        binding.btnInstallConfirm.setEnabled(requestInstallFile != null);

        origin = getIntent().getStringExtra("origin");
        final Intent requestIntent = getIntent().getParcelableExtra("intent");
        final Intent intent = requestIntent == null ? getIntent() : requestIntent;
        if (intent == null || intent.getData() == null) {
            finishAndRemoveTask();
        } else {
            requestIntentData = intent.getData();
            loadInstallInfoPermissionCompat();
        }
    }

    private void loadInstallInfoPermissionCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            loadInstallInfoApi30();
        } else {
            AppInstallerActivityPermissionsDispatcher.loadInstallInfoWithPermissionCheck(this);
        }
    }


    @SuppressLint("StaticFieldLeak")
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE})
    public void loadInstallInfo() {
        final Context context = this;
        new AsyncTask<Void, Object, AppInstallInfo>() {
            @Override
            protected AppInstallInfo doInBackground(Void... voids) {
                try {
                    requestInstallFile = getFileFromUri(requestIntentData);
                    if (requestInstallFile != null && requestInstallFile.exists()) {
                        final AppInstallInfo info = new AppInstallInfo();
                        final PackageManager pm = getPackageManager();
                        final PackageInfo archiveInfo = pm.getPackageArchiveInfo(requestInstallFile.getAbsolutePath(), PackageManager.GET_PERMISSIONS);
                        archiveInfo.applicationInfo.sourceDir = requestInstallFile.getAbsolutePath();
                        archiveInfo.applicationInfo.publicSourceDir = requestInstallFile.getAbsolutePath();
                        info.setInstalled(VLite.get().getPackageInfo(archiveInfo.packageName, 0));
                        info.setLogo(SampleUtils.convertLauncherDrawable(archiveInfo.applicationInfo.loadIcon(pm)));
                        info.setAppName(archiveInfo.applicationInfo.loadLabel(pm).toString());
                        info.setVersionCode(archiveInfo.versionCode);
                        info.setVersionName(archiveInfo.versionName);
                        info.setSize(requestInstallFile.length());
                        final List<AppPermissionInfo> appPermissions = new ArrayList<>();
                        if (archiveInfo.requestedPermissions != null) {
                            Arrays.sort(archiveInfo.requestedPermissions);
                            for (String permission : archiveInfo.requestedPermissions) {
                                final AppPermissionInfo appPermissionInfo = new AppPermissionInfo(permission, -1);
                                try {
                                    final PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
                                    final CharSequence permissionDisplayName = permissionInfo.loadLabel(pm);
                                    final CharSequence permissionDescription = permissionInfo.loadDescription(pm);
                                    appPermissionInfo.setPermissionDisplayName(permissionDisplayName == null ? permission : permissionDisplayName.toString());
                                    appPermissionInfo.setPermissionDescription(permissionDescription == null ? null : permissionDescription.toString());
                                } catch (PackageManager.NameNotFoundException ignored) {
                                }
                                if (TextUtils.isEmpty(appPermissionInfo.getPermissionDescription())) {
                                    continue;
                                }
                                appPermissions.add(appPermissionInfo);
                            }
                        }
                        info.setPermissions(appPermissions);
                        return info;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AppInstallInfo result) {
                if (result == null) {
                    Toast.makeText(context, "无法解析安装包", Toast.LENGTH_SHORT).show();
                    finishAndRemoveTask();
                } else {
                    setResult(Activity.RESULT_OK);
                    binding.btnInstallConfirm.setEnabled(requestInstallFile != null);
                    binding.tvAppPermissions.setText(getString(R.string.title_install_permission_count, result.getPermissions().size()));
                    binding.tvAppName.setText(result.getAppName());
                    binding.ivLogo.setImageDrawable(result.getLogo());
                    final StringBuilder versionString = new StringBuilder();
                    versionString.append(result.getVersionName()).append(" (").append(result.getVersionCode()).append(") ");
                    if (result.getInstalled() != null) {
                        versionString.append(" -> ").append(result.getVersionName()).append(" (").append(result.getInstalled().versionCode).append(")");
                    }
                    binding.tvAppVersionValue.setText(versionString.toString());
                    binding.tvAppFilesize.setText(getString(R.string.label_install_size_Info, FileSizeFormat.formatSize(result.getSize())));
                    binding.rvAppPermissions.setLayoutManager(new LinearLayoutManager(context));
                    final InstallerPermissionAdapter permissionAdapter = new InstallerPermissionAdapter(result.getPermissions());
                    binding.rvAppPermissions.setAdapter(permissionAdapter);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private File getFileFromUri(Uri uri) {
        try {
            // content://media/external/downloads/1000001318
            // file:///sdcard/Download/amap.apk
            // content://com.xxx.xxx/storage/emulated/0/Download/xxxx.apk
            // content://com.xxx.xxx.file.path.share/file_path/DownloadPrivate/xxxxxx.apk
            final String scheme = uri.getScheme();
            if (("content".equals(scheme) && "media".equals(uri.getAuthority()))
                    || "file".equals(scheme)
                    || TextUtils.isEmpty(origin)) {
                // content://media file:// 即使是应用内的也可以直接取
                // 没有来源说明是真机来的 直接获取
                return UriUtils.getFileFromUri(this, uri);
            } else {
                // 有来源说明是从宿主里应用来的
               final File file = UriUtils.getFileFromCursor(VLite.get().getContentResolver().query(uri, null, null, null, null));
                // 如果取出来是空的 可能是fileprovider 先复制一下到自己目录
                if (file == null) {
                    final InputStream inputStream = VLite.get().getContentResolver().openInputStream(uri);
                    final File output = new File(getExternalCacheDir(), uri.getLastPathSegment());
                    FileUtils.copyInputStreamToFile(inputStream, output);
                    return output;
                }else{
                    return file;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void loadInstallInfoApi30() {
        // 有的系统上没这个界面
        final Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo == null) {
            AppInstallerActivityPermissionsDispatcher.loadInstallInfoWithPermissionCheck(this);
        } else {
            if (Environment.isExternalStorageManager()) {
                loadInstallInfo();
            } else {
                startActivityForResult(intent, REQUEST_CODE_PERMISSION);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void requestInstallPackage() {
        final Context context = this;
        binding.btnInstallConfirm.setEnabled(false);
        binding.btnInstallConfirm.setProgressText(true, R.string.label_installing);
        new AsyncTask<Void, Object, ResultParcel>() {
            @Override
            protected ResultParcel doInBackground(Void... voids) {
                return SampleUtils.installApk(AppInstallerActivity.this, requestInstallFile.getAbsolutePath(), false);
            }

            @Override
            protected void onPostExecute(ResultParcel result) {
                installResult = result;
                binding.btnInstallConfirm.setEnabled(true);
                binding.btnInstallConfirm.setStyleType(ProgressButton.STYLE_TYPE_INFO, true);
                binding.btnInstallConfirm.setProgressText(false, R.string.label_install_finished);
                if (result.isSucceed()) {
                    Toast.makeText(context, "应用安装成功 " + (result.getData() == null ? "" : result.getData().getString(BinderEvent.KEY_PACKAGE_NAME)), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "应用安装失败 " + result.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onExternalPermissionDenied() {
        Toast.makeText(this, "请授予存储权限", Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppInstallerActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_PERMISSION == requestCode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadInstallInfo();
            } else {
                onExternalPermissionDenied();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }
}
