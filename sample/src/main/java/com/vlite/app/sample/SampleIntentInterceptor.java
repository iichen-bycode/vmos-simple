package com.vlite.app.sample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import com.vlite.app.activities.AppInstallerActivity;
import com.vlite.sdk.VLite;
import com.vlite.sdk.annotation.NonNull;
import com.vlite.sdk.application.IntentInterceptor;
import com.vlite.sdk.context.HostContext;

/**
 * intent拦截器示例
 */
public class SampleIntentInterceptor extends IntentInterceptor {

    @Override
    public Intent buildCustomRequestPermissionsIntent(@NonNull ApplicationInfo applicationInfo, String[] permissions, @NonNull Intent originIntent) {
        // 将原来的ACTION_REQUEST_PERMISSIONS重定向到AppRequestPermissionsActivity
        Intent intent = new Intent("custom.intent.action.REQUEST_PERMISSIONS");
        intent.putExtra("app_name", applicationInfo.loadLabel(HostContext.getContext().getPackageManager()));
        intent.putExtra("package_name", applicationInfo.packageName);
        intent.putExtra("permissions", permissions);
        intent.setPackage(HostContext.getPackageName());
//        return intent;
        return null;
    }

    @Override
    public Integer onInterceptStartActivity(Intent intent, Bundle args) throws Exception {
        // 重定向到自定义的安装activity
        if ((Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_INSTALL_PACKAGE.equals(intent.getAction()))
                && "application/vnd.android.package-archive".equals(intent.getType())) {
            intent.setComponent(new ComponentName(VLite.get().getServerPackageName(), AppInstallerActivity.class.getName()));
            intent.setAction(null);
            intent.putExtra("origin", VLite.get().getPackageName());
            intent.putExtra("intent", new Intent(intent));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return null;
    }
}
