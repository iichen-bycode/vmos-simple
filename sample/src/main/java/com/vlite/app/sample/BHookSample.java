package com.vlite.app.sample;

public class BHookSample {

    static {
        System.loadLibrary("mytestproject");
    }

    private static native void hookAndTestOpenat(long bhookPtr);

    public static void init() {
        // bhook测试代码
//        hookAndTestOpenat(Native.getBhookApi());
    }
}
