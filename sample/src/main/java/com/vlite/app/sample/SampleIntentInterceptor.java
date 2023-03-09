package com.vlite.app.sample;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

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
}
