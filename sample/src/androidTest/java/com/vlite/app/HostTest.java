package com.vlite.app;

import android.app.Instrumentation;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;


import com.vlite.sdk.client.hook.unittest.LiteInstrumentStarter;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HostTest {
    @Test
    public void test() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Bundle args = InstrumentationRegistry.getArguments();
        LiteInstrumentStarter liteInstrumentStarter = new LiteInstrumentStarter(instrumentation, args);
        liteInstrumentStarter.start();
    }
}