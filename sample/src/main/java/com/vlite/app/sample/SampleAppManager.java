package com.vlite.app.sample;

import android.content.ComponentName;

import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.logger.AppLogger;

import java.util.Stack;

public class SampleAppManager {
    private static final Stack<String> stack = new Stack<>();
    private static ComponentName foregroundComponentName;

    public static void onActivityLifecycle(String packageName, String methodName, String className) {
        final ComponentName lifecycleComponentName = new ComponentName(packageName, className);
        if (BinderEvent.VALUE_METHOD_NAME_ON_START.equals(methodName)) {
            foregroundComponentName = lifecycleComponentName;
            AppLogger.d("onActivityLifecycle foregroundPackageName = " + foregroundComponentName);
        } else if (BinderEvent.VALUE_METHOD_NAME_ON_STOP.equals(methodName)) {
            if (lifecycleComponentName.equals(foregroundComponentName)) {
                foregroundComponentName = null;
                AppLogger.d("onActivityLifecycle foregroundPackageName = null");
            }
        }
    }

    public static String getForegroundPackageName() {
        return foregroundComponentName == null ? null : foregroundComponentName.getPackageName();
    }
}
