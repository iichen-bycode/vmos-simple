package com.gmspace.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import com.gmspace.app.R;
import com.gmspace.sdk.GmSpaceEvent;
import com.gmspace.sdk.GmSpaceObject;
import com.gmspace.sdk.GmSpaceResultParcel;
import com.gmspace.app.adapters.InstallerPermissionAdapter;
import com.gmspace.app.bean.AppInstallInfo;
import com.gmspace.app.bean.AppPermissionInfo;
import com.gmspace.app.databinding.ActivityAppInstallerBinding;
import com.gmspace.app.sample.SampleUtils;
import com.gmspace.app.utils.FileSizeFormat;
import com.gmspace.app.view.ProgressButton;

import java.io.File;
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
    private GmSpaceResultParcel installResult;

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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            loadInstallInfoApi30();
//        } else {
//            AppInstallerActivityPermissionsDispatcher.loadInstallInfoWithPermissionCheck(this);
//        }
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
                        info.setInstalled(GmSpaceObject.getGmSpacePackageInfo(archiveInfo.packageName));
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
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void loadInstallInfoApi30() {
    }

    @SuppressLint("StaticFieldLeak")
    private void requestInstallPackage() {
        final Context context = this;
        binding.btnInstallConfirm.setEnabled(false);
        binding.btnInstallConfirm.setProgressText(true, R.string.label_installing);
        new AsyncTask<Void, Object, GmSpaceResultParcel>() {
            @Override
            protected GmSpaceResultParcel doInBackground(Void... voids) {
                return SampleUtils.installApk(AppInstallerActivity.this, requestInstallFile.getAbsolutePath(), false);
            }

            @Override
            protected void onPostExecute(GmSpaceResultParcel result) {
                installResult = result;
                binding.btnInstallConfirm.setEnabled(true);
                binding.btnInstallConfirm.setStyleType(ProgressButton.STYLE_TYPE_INFO, true);
                binding.btnInstallConfirm.setProgressText(false, R.string.label_install_finished);
                if (result.isSucceed()) {
                    Toast.makeText(context, "应用安装成功 " + (result.getData() == null ? "" : result.getData().getString(GmSpaceEvent.KEY_PACKAGE_NAME)), Toast.LENGTH_LONG).show();
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
//        AppInstallerActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
