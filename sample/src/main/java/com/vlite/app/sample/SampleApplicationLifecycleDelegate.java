package com.vlite.app.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vlite.app.utils.ViewUtils;
import com.vlite.sdk.application.ApplicationLifecycleDelegate;
import com.vlite.sdk.utils.BitmapUtils;

public class SampleApplicationLifecycleDelegate implements Application.ActivityLifecycleCallbacks, ApplicationLifecycleDelegate {

    @Override
    public void onApplicationCreate(@NonNull Application app) {
        app.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
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
    public void onActivityDestroyed(@NonNull Activity activity) {

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
