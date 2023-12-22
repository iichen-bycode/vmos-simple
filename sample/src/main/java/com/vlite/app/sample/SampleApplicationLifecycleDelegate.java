package com.vlite.app.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vlite.app.utils.ViewUtils;
import com.vlite.sdk.VLite;
import com.vlite.sdk.application.ApplicationLifecycleDelegate;
import com.vlite.sdk.context.systemservice.HostActivityManager;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.utils.BitmapUtils;

import java.util.Stack;

public class SampleApplicationLifecycleDelegate implements Application.ActivityLifecycleCallbacks, ApplicationLifecycleDelegate {
    private final Stack<Activity> stack = new Stack<>();

    @Override
    public void onApplicationCreate(@NonNull Application app) {
        app.registerActivityLifecycleCallbacks(this);

        // 注册虚拟广播 让主进程可以发指令到app进程
        final String action = "command_" + app.getPackageName();
        VLite.get().registerBinderBroadcastReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String commandId = intent.getStringExtra("command_id");
                handleCommandId(commandId);
            }
        }, new IntentFilter(action));
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void handleCommandId(String commandId) {
        final Activity activity = peekActivity();
        if (activity == null) {
            return;
        }
        if ("force_portrait".equals(commandId)) {
            HostActivityManager.get().setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if ("force_landscape".equals(commandId)) {
            HostActivityManager.get().setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if ("force_pip".equals(commandId)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                activity.enterPictureInPictureMode();
            }
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        stack.push(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        asyncCacheSnapshot(activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
        stack.remove(activity);
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    /**
     * 返回栈顶的Activity并移除
     */
    private Activity peekActivity() {
        synchronized (this) {
            return !stack.isEmpty() ? stack.peek() : null;
        }
    }

    /**
     * 异步缓存快照
     */
    @SuppressLint("StaticFieldLeak")
    private void asyncCacheSnapshot(Activity activity) {
        final View v = activity.getWindow().getDecorView();
        if (v != null) {
            v.post(() -> new AsyncTask<Void, Object, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        final Bitmap bitmap = ViewUtils.toBitmap(v);
                        if (bitmap != null) {
                            final String filepath = SampleUtils.getSnapshotCacheFile(activity.getPackageName()).getAbsolutePath();
                            BitmapUtils.toFile(bitmap, filepath);
                            Log.d("------------->", activity + " snapshot -> " + filepath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
        }
    }
}
