package com.gmspace.app.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;

public class DateFormatUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat FILE_KEY_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static String createFileKey() {
        return FILE_KEY_FORMAT.format(System.currentTimeMillis());
    }

    public static String toCurrentTimeString() {
        return DATE_FORMAT.format(System.currentTimeMillis());
    }

    public static String toString(long time) {
        return DATE_FORMAT.format(time);
    }

    public static String toDurationString(long time) {
        long hour = time / DateUtils.HOUR_IN_MILLIS;
        long minute = time % DateUtils.HOUR_IN_MILLIS / DateUtils.MINUTE_IN_MILLIS;
        long second = time % DateUtils.HOUR_IN_MILLIS % DateUtils.MINUTE_IN_MILLIS / DateUtils.SECOND_IN_MILLIS;
        long millis = time % DateUtils.HOUR_IN_MILLIS % DateUtils.MINUTE_IN_MILLIS % DateUtils.SECOND_IN_MILLIS;
        String secondString = String.format("%d秒%d毫秒", second, millis);
        String minuteString = String.format("%d分钟%s", minute, secondString);
        String hourString = String.format("%d小时%s", hour, minuteString);
        if (time < DateUtils.SECOND_IN_MILLIS) {
            return millis + "毫秒";
        } else if (time < DateUtils.MINUTE_IN_MILLIS) {
            return secondString;
        } else if (time < DateUtils.HOUR_IN_MILLIS) {
            return minuteString;
        } else {
            return hourString;
        }
    }
}
