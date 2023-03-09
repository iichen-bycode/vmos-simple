package com.vlite.app.sample;

import com.tencent.mmkv.MMKV;
import com.vlite.sdk.logger.AppLogger;
import com.vlite.sdk.model.DeviceEnvInfo;
import com.vlite.sdk.utils.GsonUtils;

public class SampleDeviceUtils {
    private static final MMKV deviceMMKV = MMKV.mmkvWithID("virtual_device");

    public static DeviceEnvInfo getVirtualDeviceInfo() {
        final String json = deviceMMKV.getString("virtual_device", null);
        AppLogger.d(json);
        return GsonUtils.toObject(json, DeviceEnvInfo.class);
    }

    public static void putVirtualDeviceInfo(DeviceEnvInfo deviceInfo) {
        deviceMMKV.putString("virtual_device", GsonUtils.toJson(deviceInfo));
    }
}
