package com.gmspace.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.gmspace.sdk.GmSpaceObject;
import com.gmspace.sdk.IGmSpaceInitCallBack;
import com.gmspace.sdk.model.GmSpace32BitExtConfig;
import com.gmspace.sdk.proxy.GmSpaceHostContext;
import com.ssy185.app.sdk.GMTBOX;
import com.ssy185.sdk.common.base.inerface.GmtInitListener;
import com.tencent.mmkv.MMKV;

public class SampleApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        GmSpaceObject.attachBaseContext(base,true);
        GmSpaceObject.attachBaseContext(base);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (GmSpaceHostContext.isMainProcess()) {
            MMKV.initialize(this);
        }

        GmSpaceObject.initialize(this, "fIyzKzyNNBEw1Hnn", "3ppgrZzdkRhunw", new IGmSpaceInitCallBack() {
//        GmSpaceObject.initialize(this, "VoDjjBfYHFFARIVk", "EspxOgQpOWapok", new IGmSpaceInitCallBack() {
            @Override
            public void initResult(boolean b, int i, String s) {
                Log.i("csc", "初始化有没有成功" + b);
            }
        });

        GmSpaceObject.set32BitExtConfig(new GmSpace32BitExtConfig(
                "com.gmspace.sdk",
                "com.gmspace.ext.PluginInstallActivity",
                "com.gmspace.ext.PluginLaunchActivity",
                "com.gmspace.ext.PluginUnInstallActivity"
        ));

        MMKV.initialize(this);
        GMTBOX.enableLog();
        GMTBOX.init(this, "7Txf2MMpYus36EPd", "GoCnqGWXcwmBYX", new GmtInitListener() {
            @Override
            public void onFail(int i) {
                Log.d("iichen",">>>>>>>>>>>>>>>>> 初始化失败  $p0");
            }

            @Override
            public void onSuccess() {
                Log.d("iichen",">>>>>>>>>>>>>>>>> 初始化成功");

            }
        });
    }
}
