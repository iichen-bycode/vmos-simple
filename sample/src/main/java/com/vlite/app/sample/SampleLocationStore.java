package com.vlite.app.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.vlite.sdk.context.HostContext;

/**
 * 持久化虚拟定位信息的示例
 */
public class SampleLocationStore {

    private static SharedPreferences mLocationStore;

    private static SharedPreferences getSharedPreferences() {
        if (mLocationStore == null) {
            mLocationStore = HostContext.getContext().getSharedPreferences("location", Context.MODE_MULTI_PROCESS);

        }
        return mLocationStore;
    }

    public static void clear() {
        getSharedPreferences().edit().clear().apply();
    }


    public static void setFakeLocation(Location location) {
        getSharedPreferences().edit()
                .putString("provider", location.getProvider())
                .putFloat("latitude", (float) location.getLatitude())
                .putFloat("longitude", (float) location.getLongitude())
                .apply();
    }


    public static Location getFakeLocation() {
        final SharedPreferences preferences = getSharedPreferences();
        final String provider = preferences.getString("provider", null);
        if (!TextUtils.isEmpty(provider) && preferences.contains("latitude") && preferences.contains("longitude")) {
            final Location location = new Location(provider);
            location.setLatitude(preferences.getFloat("latitude", 0f));
            location.setLongitude(preferences.getFloat("longitude", 0f));
            //            location.setLongitude(116.275747);
//            location.setLatitude(40.0006376);
//            location.setAltitude(66.78003831673414);
            location.setSpeed(10f); // 这里设置为0时设置了虚拟地址可能也会跳转到真实地址
//            location.setBearing(0);
            location.setAccuracy(1f);
            location.setTime(System.currentTimeMillis());
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            return location;
        }
        return null;
    }

}
