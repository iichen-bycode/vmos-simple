package com.gmspace.app.sample;

import android.content.ComponentName;
import android.util.Log;

import com.gmspace.sdk.GmSpaceEvent;

import java.util.Stack;

public class SampleAppManager {
    private static final Stack<String> stack = new Stack<>();
    private static ComponentName foregroundComponentName;

    public static void onActivityLifecycle(String packageName, String methodName, String className) {
        final ComponentName lifecycleComponentName = new ComponentName(packageName, className);
        if (GmSpaceEvent.VALUE_METHOD_NAME_ON_START.equals(methodName)) {
            foregroundComponentName = lifecycleComponentName;
            Log.d("iichen","onActivityLifecycle foregroundPackageName = " + foregroundComponentName);
        } else if (GmSpaceEvent.VALUE_METHOD_NAME_ON_STOP.equals(methodName)) {
            if (lifecycleComponentName.equals(foregroundComponentName)) {
                foregroundComponentName = null;
                Log.d("iichen","onActivityLifecycle foregroundPackageName = null");
            }
        }
    }

    public static String getForegroundPackageName() {
        return foregroundComponentName == null ? null : foregroundComponentName.getPackageName();
    }
}
