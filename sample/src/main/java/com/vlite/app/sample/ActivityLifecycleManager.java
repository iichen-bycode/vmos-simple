package com.vlite.app.sample;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class ActivityLifecycleManager {

    private static class ActivityLifecycleManagerHolder {
        private static final ActivityLifecycleManager instance = new ActivityLifecycleManager();
    }

    private ActivityLifecycleManager() {
    }

    public static ActivityLifecycleManager getInstance() {
        return ActivityLifecycleManagerHolder.instance;
    }

    private final Stack<Activity> stack = new Stack<>();

    public void register(Application application) {
        application.registerActivityLifecycleCallbacks(new SampleApplicationLifecycleDelegate() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                pushActivity(activity);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                removeActivity(activity);
            }
        });
    }

    public void pushActivity(Activity activity) {
        synchronized (this) {
            stack.push(activity);
        }
    }

    public void removeActivity(Activity activity) {
        synchronized (this) {
            try {
                stack.remove(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回栈顶的Activity但不移除
     */
    public Activity peekActivity() {
        synchronized (this) {
            return stack != null && !stack.isEmpty() ? stack.peek() : null;
        }
    }

}