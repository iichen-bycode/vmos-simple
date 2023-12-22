package com.vlite.app.sample;

import com.vlite.sdk.event.BinderEvent;
import com.vlite.sdk.logger.AppLogger;

import java.util.Stack;

public class SampleAppManager {
    private static final Stack<String> stack = new Stack<>();
    private static String foregroundPackageName ;

    public static void onActivityLifecycle(String packageName, String methodName) {
//        AppLogger.d("onActivityLifecycle packageName = " + packageName+", methodName = "+methodName);
        if (BinderEvent.VALUE_METHOD_NAME_ON_START.equals(methodName)) {
            foregroundPackageName = packageName;
            AppLogger.d("onActivityLifecycle foregroundPackageName = " + foregroundPackageName);
        } else if (BinderEvent.VALUE_METHOD_NAME_ON_STOP.equals(methodName)) {
            if (foregroundPackageName.equals(packageName)) {
                foregroundPackageName = null;
                AppLogger.d("onActivityLifecycle foregroundPackageName = null");
            }
        }
    }

    public static String getForegroundPackageName() {
        return foregroundPackageName;
    }
}
