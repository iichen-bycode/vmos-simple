package com.vlite.app.sample;

import android.app.PendingIntent;
import android.location.ILocationListener;
import android.location.LastLocationRequest;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.vlite.sdk.server.virtualservice.location.ILocationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义LocationManagerService的示例
 * 此类运行与server进程
 */
public class SampleLocationManagerService extends ILocationManager.Stub {
    private final String TAG = "SampleLocationManager";

    private final Map<IBinder, LocationCallbackInfo> mListeners = new HashMap<>();

    public SampleLocationManagerService() {
        startCallbackThread();
    }

    private void startCallbackThread() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        batchCallbackFakeLocationChanged();
                        SystemClock.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void callbackLocationChanged(ILocationListener listener, Location location) {
        try {
            final Class<?> cls = listener.getClass();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                final Method method = cls.getMethod("onLocationChanged", List.class, Class.forName("android.os.IRemoteCallback"));
                method.invoke(listener, Arrays.asList(location), null);
            } else {
                final Method method = cls.getMethod("onLocationChanged", Location.class);
                method.invoke(listener, location);
            }
            Log.d(TAG, "onLocationChanged -> " + listener + ", location = " + location);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void batchCallbackFakeLocationChanged() {
        if (mListeners.isEmpty()) {
            return;
        }
        final Location fakeLocation = SampleLocationStore.getFakeLocation();
        if (fakeLocation == null) {
//            Log.w(TAG, "fake location is null");
            return;
        }

        final List<LocationCallbackInfo> callbacks = new ArrayList<>();
        synchronized (mListeners) {
            final Set<Map.Entry<IBinder, LocationCallbackInfo>> entries = mListeners.entrySet();
            for (Map.Entry<IBinder, LocationCallbackInfo> entry : entries) {
//            final IBinder key = entry.getKey();
                final LocationCallbackInfo value = entry.getValue();
                if (value.currentUpdates >= value.maxUpdates) {
                    continue;
                }
                final long intervalTimestamp = SystemClock.uptimeMillis() - value.callbackTimestamp;
                if (value.callbackTimestamp != 0 && intervalTimestamp < value.intervalMillis) {
                    continue;
                }
                callbacks.add(value);
            }
        }

        for (LocationCallbackInfo info : callbacks) {
            try {
                info.callbackTimestamp = SystemClock.uptimeMillis();
                info.currentUpdates++;
                callbackLocationChanged(info.listener, fakeLocation);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private <T> T getFieldValue(LocationRequest request, String fieldName, T defaultValue) {
        try {
            final Class<?> locationRequestClass = request.getClass();
            final Field field = locationRequestClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(request);
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        }
        return defaultValue;
    }

    private void requestLocationUpdates(LocationRequest request, ILocationListener listener) {
        if (listener == null) {
            return;
        }
        try {

            final int maxUpdates = getFieldValue(request, "mMaxUpdates", 1);
            final long interval = getFieldValue(request, "mInterval", Long.MAX_VALUE);
            final LocationCallbackInfo callbackInfo = new LocationCallbackInfo(maxUpdates, interval, listener);
            final IBinder iBinder = listener.asBinder();
            synchronized (mListeners) {
                mListeners.put(iBinder, callbackInfo);
            }
            Log.d(TAG, "requestLocationUpdates -> " + iBinder + ", " + callbackInfo);

            iBinder.linkToDeath(() -> {
                removeUpdates(iBinder);
            }, 0);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void removeUpdates(ILocationListener listener) {
        if (listener == null) {
            return;
        }
        final IBinder iBinder = listener.asBinder();
        removeUpdates(iBinder);
    }

    private void removeUpdates(IBinder iBinder) {
        synchronized (mListeners) {
            mListeners.remove(iBinder);
        }
        Log.d(TAG, "removeUpdates -> " + iBinder);
    }

    private Location getLastLocation() {
        return SampleLocationStore.getFakeLocation();
    }

    @Override
    public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) throws RemoteException {
        removeUpdates(listener);
    }

    @Override
    public void locationCallbackFinished(ILocationListener listener) throws RemoteException {
    }

    @Override
    public Location getLastLocation(LocationRequest request, String packageName, String featureId) throws RemoteException {
        return getLastLocation();
    }

    @Override
    public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName, String featureId, String listenerId) throws RemoteException {
        requestLocationUpdates(request, listener);
    }

    @Override
    public Location getLastLocationApi31(String provider, LastLocationRequest request, String packageName, String attributionTag) throws RemoteException {
        return getLastLocation();
    }

    @Override
    public void registerLocationListener(String provider, LocationRequest request, ILocationListener listener, String packageName, String attributionTag, String listenerId) throws RemoteException {
        requestLocationUpdates(request, listener);
    }

    @Override
    public void unregisterLocationListener(ILocationListener listener) throws RemoteException {
        removeUpdates(listener);
    }

    private static class LocationCallbackInfo {
        int maxUpdates;
        int currentUpdates;
        long intervalMillis;
        long callbackTimestamp;
        ILocationListener listener;

        public LocationCallbackInfo(int maxUpdates, long intervalMillis, ILocationListener listener) {
            this.maxUpdates = maxUpdates;
            this.intervalMillis = intervalMillis;
            this.listener = listener;
        }

        @Override
        public String toString() {
            return "LocationCallbackInfo{" +
                    "maxUpdates=" + maxUpdates +
                    ", currentUpdates=" + currentUpdates +
                    ", intervalMillis=" + intervalMillis +
                    ", callbackTimestamp=" + callbackTimestamp +
                    ", listener=" + listener +
                    '}';
        }
    }
}
