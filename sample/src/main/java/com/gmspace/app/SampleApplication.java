package com.gmspace.app;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.gmspace.sdk.GmSpaceObject;
import com.gmspace.sdk.IGmSpaceInitCallBack;
import com.gmspace.sdk.proxy.GmSpaceHostContext;
import com.tencent.mmkv.MMKV;

public class SampleApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // 使用默认配置执行准备工作
//        VLite.attachBaseContext(base, BuildConfig.DEBUG);

        // 使用自定义配置执行准备工作
        // 以下配置供参考 请根据实际场景进行配置

        GmSpaceObject.attachBaseContext(base);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        if (GmSpaceHostContext.isMainProcess()) {
            MMKV.initialize(this);
        }

        GmSpaceObject.initialize(this, "fIyzKzyNNBEw1Hnn", "3ppgrZzdkRhunw", new IGmSpaceInitCallBack() {
            @Override
            public void initResult(boolean b, int i, String s) {
                Log.i("csc","初始化有没有成功"+b);

            }
        });
    }
}
