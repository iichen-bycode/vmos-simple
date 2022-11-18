package com.vlite.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class ServiceUtils {

    private static Notification createForegroundNotification(Service service, String id, String channelName, int icon, String title) {
        NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[0]);
            channel.setSound(null, null);
            channel.enableVibration(false);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(service, id)
                .setVibrate(null).setSound(null)
                .setSmallIcon(icon).setContentTitle(title)
                .setAutoCancel(false).setOngoing(true).build();
    }

    public static void notifyForeground(@NonNull Service service, @NonNull String id, @NonNull String channelName, int icon, @NonNull String title) {
        NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id.hashCode(), createForegroundNotification(service, id, channelName, icon, title));
    }

    public static void startForeground(@NonNull Service service, @NonNull String id, @NonNull String channelName, int icon, @NonNull String title) {
        service.startForeground(id.hashCode(), createForegroundNotification(service, id, channelName, icon, title));
    }
}
