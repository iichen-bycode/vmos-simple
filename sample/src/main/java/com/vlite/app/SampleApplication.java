package com.vlite.app;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.tencent.mmkv.MMKV;
import com.vlite.sdk.LiteConfig;
import com.vlite.sdk.VLite;
import com.vlite.sdk.context.HostContext;

public class SampleApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        VLite.attachBaseContext(base, new LiteConfig.Builder()
                // 日志打印跟随app编译类型
                .setLoggerEnabled(BuildConfig.DEBUG)
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
