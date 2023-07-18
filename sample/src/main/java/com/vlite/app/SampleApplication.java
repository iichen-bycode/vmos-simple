package com.vlite.app;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.tencent.mmkv.MMKV;
import com.vlite.app.sample.PackageManagerProxySample;
import com.vlite.app.sample.SampleLocationManagerService;
import com.vlite.app.sample.TelephoneManagerProxySample;
import com.vlite.app.sample.WifiManagerProxySample;
import com.vlite.sdk.LiteConfig;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;
import com.vlite.sdk.context.ServiceContext;
import com.vlite.sdk.logger.AppLogger;

public class SampleApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // 使用默认配置执行准备工作
//        VLite.attachBaseContext(base, BuildConfig.DEBUG);

        // 使用自定义配置执行准备工作
        // 以下配置供参考 请根据实际场景进行配置
        VLite.attachBaseContext(base, new LiteConfig.Builder()
                // 指定服务端包名
//                .setServerPackageName(BuildConfig.APPLICATION_ID)
                // 日志配置
                .setLoggerConfig(new AppLogger.Config(BuildConfig.DEBUG)
                        // 日志增加mars支持
//                        .addLogger(new MarsLogger(this))
                )
                // 注册自定义服务
//                .registerCustomService(ServiceContext.LOCATION_SERVICE, SampleLocationManagerService.class)
                // 设置系统服务客户端代理
//                .registerSystemServiceClientProxy(ServiceContext.PACKAGE_SERVICE, PackageManagerProxySample.class)
//                .registerSystemServiceClientProxy(ServiceContext.TELEPHONY_SERVICE, TelephoneManagerProxySample.class)
//                .registerSystemServiceClientProxy(ServiceContext.WIFI_SERVICE, WifiManagerProxySample.class)
                .build());
    }


    @Override
    public void onCreate() {
        super.onCreate();
        VLite.initialize(this);

        if (HostContext.isMainProcess()) {
            MMKV.initialize(this);
        }
    }
}
