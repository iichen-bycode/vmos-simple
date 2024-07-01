//package com.vlite.app.utils;
//
//import android.content.pm.ParceledListSlice;
//import android.text.TextUtils;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.vlite.sdk.logger.AppLogger;
//import com.vlite.sdk.reflect.android.content.pm.Ref_ParceledListSlice;
//
//import java.lang.reflect.Array;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//import de.robv.android.xposed.XC_MethodHook;
//import de.robv.android.xposed.XposedHelpers;
//
//public class XposedUtils {
//    public static final String TAG = "XposedUtils";
//
//    public static void hookAllMethods(String className, ClassLoader classLoader, String methodName, XC_MethodHook... methodHooks) {
//        try {
//            hookAllMethods(classLoader.loadClass(className), methodName, methodHooks);
//        } catch (Throwable e) {
//            AppLogger.wt(TAG, "hookAllMethods " + className + "." + methodName + " failed", e);
//        }
//    }
//
//    private static XC_MethodHook[] appendPresetMethodHook(Class<?> cls, XC_MethodHook... methodHooks) {
//        final XC_MethodHook[] newMethodHooks;
//        if (methodHooks == null || methodHooks.length == 0) {
//            newMethodHooks = new XC_MethodHook[]{new PrintMethodHook()};
//        } else {
//            newMethodHooks = methodHooks;
//        }
//        for (XC_MethodHook mh : newMethodHooks) {
//            if (mh instanceof AbstractMethodHook) {
//                ((AbstractMethodHook) mh).setHookClassName(cls.getName());
//            }
//        }
//        return newMethodHooks;
//    }
//
//    private static Object[] createParameterTypesAndCallback(Class<?>[] parameterTypes, XC_MethodHook[] methodHooks) {
//        final Object[] parameterTypesAndCallback = new Object[parameterTypes.length + 1];
//        System.arraycopy(parameterTypes, 0, parameterTypesAndCallback, 0, parameterTypes.length);
//        parameterTypesAndCallback[parameterTypesAndCallback.length - 1] = new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                for (XC_MethodHook mh : methodHooks) {
//                    mh.callBeforeHookedMethod(param);
//                }
//            }
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                for (XC_MethodHook mh : methodHooks) {
//                    mh.callAfterHookedMethod(param);
//                }
//            }
//        };
//        return parameterTypesAndCallback;
//    }
//
//    public static void hookAllMethods(Class<?> clazz, String methodName, XC_MethodHook... methodHooks) {
//        try {
//            final XC_MethodHook[] newMethodHooks = appendPresetMethodHook(clazz, methodHooks);
//            final Method[] methods = findMethods(clazz, methodName);
//            for (Method m : methods) {
//                try {
//
//                    final Object[] parameterTypesAndCallback = createParameterTypesAndCallback(m.getParameterTypes(), newMethodHooks);
//                    XposedHelpers.findAndHookMethod(clazz, m.getName(), parameterTypesAndCallback);
//                } catch (Throwable e) {
//                    AppLogger.wt(TAG, "hookMethod " + clazz + "." + m.getName() + " failed, " + e.getMessage());
//                }
//            }
//        } catch (Throwable e) {
//            AppLogger.wt(TAG, "hookAllMethods " + clazz + "." + methodName + " failed", e);
//        }
//    }
//
//
//    public static void hookAllConstructors(String className, ClassLoader classLoader, XC_MethodHook... methodHooks) {
//        try {
//            hookAllConstructors(classLoader.loadClass(className), methodHooks);
//        } catch (Throwable e) {
//            AppLogger.wt(TAG, "hookAllConstructors " + className + " failed", e);
//        }
//    }
//
//    public static void hookAllConstructors(Class<?> clazz, XC_MethodHook... methodHooks) {
//        try {
//            final XC_MethodHook[] newMethodHooks = appendPresetMethodHook(clazz, methodHooks);
//            final Constructor<?>[] constructors = findConstructors(clazz);
//            for (Constructor<?> c : constructors) {
//                final Object[] parameterTypesAndCallback = createParameterTypesAndCallback(c.getParameterTypes(), newMethodHooks);
//                XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
//            }
//        } catch (Throwable e) {
//            AppLogger.wt(TAG, "hookAllConstructors " + clazz + " failed", e);
//        }
//    }
//
//    public static @NonNull Method[] findMethods(Class<?> clazz, @Nullable String methodName) {
//        final Map<String, Method> hookMethods = new LinkedHashMap<>();
//        try {
//            if (clazz != null) {
//                final List<Method> allMethods = new ArrayList<>();
//                allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
//                allMethods.addAll(Arrays.asList(clazz.getMethods()));
//                for (Method m : allMethods) {
//                    if (methodName == null || methodName.equals(m.getName())) {
//                        final String methodKey = methodKey(clazz, m);
//                        if (!hookMethods.containsKey(methodKey)) {
//                            hookMethods.put(methodKey, m);
//                        }
//                    }
//                }
//            }
//        } catch (Throwable e) {
//            AppLogger.wt(TAG, "findMethods " + clazz + "." + methodName + " failed", e);
//        }
//        return hookMethods.values().toArray(new Method[0]);
//    }
//
//    public static @NonNull Constructor<?>[] findConstructors(Class<?> clazz) {
//        final Map<String, Constructor<?>> hookMethods = new LinkedHashMap<>();
//        try {
//            if (clazz != null) {
//                final List<Constructor<?>> allMethods = new ArrayList<>();
//                allMethods.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
//                allMethods.addAll(Arrays.asList(clazz.getConstructors()));
//                for (Constructor<?> c : allMethods) {
//                    final String key = constructorKey(clazz, c);
//                    if (!hookMethods.containsKey(key)) {
//                        hookMethods.put(key, c);
//                    }
//                }
//            }
//        } catch (Throwable e) {
//            AppLogger.wt(TAG, "findMethods " + clazz + " failed", e);
//        }
//        return hookMethods.values().toArray(new Constructor<?>[0]);
//    }
//
//    private static String methodKey(Class<?> clazz, Method method) {
//        return clazz.getName() + "#" + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
//    }
//
//    private static String constructorKey(Class<?> clazz, Constructor<?> method) {
//        return clazz.getName() + "#" + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")";
//    }
//
//    public static class PrintMethodHook extends AbstractMethodHook {
//        private final boolean isPrintStackTrace;
//        private final String tag;
//
//        public PrintMethodHook() {
//            this(false);
//        }
//
//        public PrintMethodHook(boolean print) {
//            this(print, "");
//        }
//
//        public PrintMethodHook(boolean print, String tag) {
//            this.isPrintStackTrace = print;
//            this.tag = tag;
//        }
//
//        @Override
//        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//            AppLogger.dt(TAG, tag + " beforeHookedMethod " + toMethodString(param));
//        }
//
//        @Override
//        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//            final String stackTrace = isPrintStackTrace ? ", stackTrace = \n" + Log.getStackTraceString(new Throwable()) : "";
//            final Class<?> resultClass = param.getResult() == null ? null : param.getResult().getClass();
//            final String resultString = toObjectString(param.getResult());
//            final String newResultString = resultString.length() > 1024 ? resultString.substring(0, 1024) + "..." : resultString;
//            AppLogger.dt(TAG, tag + " afterHookedMethod " + toMethodString(param) + " = (" + resultClass + ") = " + newResultString + stackTrace);
//        }
//
//        protected String toMethodString(MethodHookParam param) {
//            final String separator = ", ";
//            final String hookClassOrObject = param.thisObject == null ? getHookClassName()
//                    : param.thisObject.getClass().getName() + "@" + Integer.toHexString(param.thisObject.hashCode());
//            final StringBuilder sb = new StringBuilder("[").append(hookClassOrObject).append("]").append(" ")
//                    .append(getHookClassName()).append(".").append(param.method.getName());
//            sb.append("(");
//            if (param.args != null && param.args.length > 0) {
//                sb.append("[");
//                for (Object arg : param.args) {
//                    final String argString = Objects.toString(arg);
//                    final String newArgString = argString.length() > 256 ? argString.substring(0, 256) + "..." : argString;
//                    sb.append(newArgString).append(separator);
//                }
//                // 删除分隔符
//                for (int i = 0; i < separator.length(); i++) {
//                    sb.deleteCharAt(sb.length() - 1);
//                    sb.deleteCharAt(sb.length() - 1);
//                }
//                sb.append("]");
//            }
//            sb.append(")");
//            return sb.toString();
//        }
//
//        private String toObjectString(Object ret) {
//            if (ret instanceof Object[]) {
//                return Arrays.toString((Object[]) ret);
//            }
//            if (ret instanceof ParceledListSlice) {
//                return Objects.toString(((ParceledListSlice<?>) ret).getList());
//            }
//            return Objects.toString(ret);
//        }
//
//        protected String getTag() {
//            return TAG;
//        }
//    }
//
//    public static class DirectResultMethodHook extends AbstractMethodHook {
//        private final Object result;
//
//        public DirectResultMethodHook(Object result) {
//            this.result = result;
//        }
//
//        @Override
//        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//            param.setResult(result);
//        }
//    }
//
//    public static abstract class AbstractMethodHook extends XC_MethodHook {
//        private String hookClassName;
//
//        public void setHookClassName(String hookClassName) {
//            this.hookClassName = hookClassName;
//        }
//
//        public String getHookClassName() {
//            return hookClassName;
//        }
//    }
//}
//
