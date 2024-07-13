package com.gmspace.app.utils;

import android.os.Build;
import android.os.Process;

import java.lang.reflect.Method;

public class RuntimeUtils {

    public static boolean is64bit() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Process.is64Bit();
            } else {
                // 获取 VMRuntime 类
                Class<?> vmRuntimeClass = Class.forName("dalvik.system.VMRuntime");

                // 获取 VMRuntime 的实例（单例模式）
                Method getVMRuntimeMethod = vmRuntimeClass.getDeclaredMethod("getRuntime");
                getVMRuntimeMethod.setAccessible(true);
                Object vmRuntimeInstance = getVMRuntimeMethod.invoke(null);

                // 获取 is64Bit 方法
                Method is64BitMethod = vmRuntimeClass.getDeclaredMethod("is64Bit");
                is64BitMethod.setAccessible(true);

                // 调用 is64Bit 方法并获取返回值
                return (boolean) is64BitMethod.invoke(vmRuntimeInstance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
