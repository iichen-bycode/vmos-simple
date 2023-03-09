package com.vlite.app.sample;

import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.Window;

import com.vlite.sdk.VLite;
import com.vlite.sdk.client.hook.service.app.InstrumentationLiteApp;
import com.vlite.sdk.context.HostContext;

import java.lang.reflect.Method;

public class InputManagerSample {
    private static InputManager mService;

    private static InputManager getService() {
        if (mService == null) {
            mService = (InputManager) HostContext.getContext().getSystemService(Context.INPUT_SERVICE);
        }
        return mService;
    }

    public static boolean injectInputEvent(Window window, MotionEvent event) {
        Log.d("------------>", "------------------->"+window);
//        window.injectInputEvent(event);

        new Thread(){
            @Override
            public void run() {

                VLite.get().getInstrumentation().sendPointerSync(event);
            }
        }.start();
//        return injectInputEvent(event, 0);
        return true;
    }

    public static boolean injectInputEvent(InputEvent event, int mode) {
        try {
            final Method method = InputManager.class.getMethod("injectInputEvent", InputEvent.class, int.class);
            method.setAccessible(true);
            method.invoke(getService(), event, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
