package com.vlite.app.sample;

import android.util.Log;

import com.vlite.sdk.application.MethodOverrideHandler;
import com.vlite.sdk.application.SystemServiceClientProxy;
import com.vlite.sdk.context.HostContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 此示例演示了如何在app进程覆盖原本的函数调用
 * 此类运行于app进程
 */
public class TelephoneManagerProxySample extends SystemServiceClientProxy {
    private final Map<String, MethodOverrideHandler> methods = new HashMap<>();

    public TelephoneManagerProxySample() {
        // 覆盖getAllCellInfo原本逻辑 当参数是宿主包名时返回null
        methods.put("getAllCellInfo", new MethodOverrideHandler() {
            @Override
            public Object doInvoke(Object obj, Method method, Object[] args) throws Throwable {
                // PackageInfo getPackageInfo(String packageName, int flags, int userId);
                final String packageName = (String) args[0];
                if (HostContext.getPackageName().equals(packageName)) {
                    return null;
                } else {
                    return super.doInvoke(obj, method, args);
                }
            }
        });

        // 覆盖getCellLocation原本逻辑 当参数是宿主包名时返回null
        methods.put("getCellLocation", new MethodOverrideHandler() {
            @Override
            public Object doInvoke(Object obj, Method method, Object[] args) throws Throwable {
                // PackageInfo getPackageInfo(String packageName, int flags, int userId);
                final String packageName = (String) args[0];
                if (HostContext.getPackageName().equals(packageName)) {
                    return null;
                } else {
                    return super.doInvoke(obj, method, args);
                }
            }
        });
    }

    @Override
    public Map<String, MethodOverrideHandler> getIServiceMethodProxies() {
        return methods;
    }
}
