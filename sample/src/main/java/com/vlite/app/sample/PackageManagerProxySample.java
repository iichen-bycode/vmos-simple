package com.vlite.app.sample;

import android.content.pm.PackageInfo;

import com.vlite.sdk.annotation.NonNull;
import com.vlite.sdk.annotation.Nullable;
import com.vlite.sdk.application.MethodOverrideHandler;
import com.vlite.sdk.application.SystemServiceClientProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 此示例演示了如何在app进程覆盖原本的函数调用
 * 此类运行于app进程
 */
public class PackageManagerProxySample extends SystemServiceClientProxy {
    private final Map<String, MethodOverrideHandler> methods = new HashMap<>();

    public PackageManagerProxySample() {
        // 覆盖getPackageInfo原本逻辑 修复PackageInfo versionName
        methods.put("getPackageInfo", new MethodOverrideHandler() {
            @Override
            public Object afterInvoke(@NonNull Object obj, @NonNull Method method, Object[] params, @Nullable Object retVal) throws Throwable {
                Object result = super.afterInvoke(obj, method, params, retVal);
                if (result instanceof PackageInfo) {
                    ((PackageInfo) result).versionName = "9.9.9-custom";
                }
                return result;
            }
        });
    }

    @Override
    public Map<String, MethodOverrideHandler> getIServiceMethodProxies() {
        return methods;
    }
}
