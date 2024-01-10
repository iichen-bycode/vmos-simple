package com.vlite.app.sample;

import android.content.pm.PackageManager;

import com.vlite.sdk.VLite;
import com.vlite.sdk.application.MethodOverrideHandler;
import com.vlite.sdk.application.SystemServiceClientProxy;
import com.vlite.sdk.logger.AppLogger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 此示例演示了如何在app进程覆盖原本的函数调用
 * 此类运行于app进程
 */
public class ActivityManagerProxySample extends SystemServiceClientProxy {
    private final Map<String, MethodOverrideHandler> methods = new HashMap<>();

    public ActivityManagerProxySample() {
        // 覆盖getPackageInfo原本逻辑 当参数是宿主包名时返回null
        methods.put("checkPermission", new MethodOverrideHandler() {
            @Override
            public Object afterInvoke(Object obj, Method method, Object[] args, Object retVal) throws Throwable {
                try {
                    String permission = (String) args[0];
                    String packageName = VLite.get().getPackageName();
                    if ("com.viber.voip".equals(packageName) && "android.permission.READ_CALL_LOG".equals(permission)) {
                        return PackageManager.PERMISSION_DENIED;
                    }
                } catch (Exception e) {
                    AppLogger.e(e);
                }
                return retVal;
            }
        });
    }

    @Override
    public Map<String, MethodOverrideHandler> getIServiceMethodProxies() {
        return methods;
    }
}
