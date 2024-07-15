package com.gmspace.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.gmspace.app.R;
import com.gmspace.app.utils.ServiceUtils;

/**
 * 用于服务进程意外死亡
 * 软件服务进行保活避免系统回收
 */
public class AppKeepAliveService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceUtils.startForeground(this, "AppKeepAlive", "AppKeepAlive", R.mipmap.icon_launcher, "服务正在运行");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
