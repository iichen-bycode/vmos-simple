package com.vlite.app.sample;

import androidx.annotation.Nullable;
import com.vlite.sdk.application.DeviceEnvOverrider;
import com.vlite.sdk.model.DeviceEnvInfo;

public class SampleDeviceEnvOverrider implements DeviceEnvOverrider {

    @Override
    public @Nullable DeviceEnvInfo overrideDeviceEnvInfo(@Nullable DeviceEnvInfo base) {
        final DeviceEnvInfo deviceEnvInfo = new DeviceEnvInfo();
        deviceEnvInfo.putBuildField("BRAND", "xiaomi");
        return deviceEnvInfo;
    }
}
