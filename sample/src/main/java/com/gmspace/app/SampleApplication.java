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
