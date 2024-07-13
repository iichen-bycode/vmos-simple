package com.gmspace.app;

import android.app.Instrumentation;
import android.os.Bundle;

import com.gmspace.sdk.proxy.GmSpaceUtils;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;


import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HostTest {
    @Test
    public void test() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Bundle args = InstrumentationRegistry.getArguments();
        GmSpaceUtils.startUnitTest(instrumentation, args);
    }
}