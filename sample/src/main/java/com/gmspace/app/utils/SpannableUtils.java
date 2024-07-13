package com.gmspace.app.utils;

import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

public class SpannableUtils {

    public static SpannableStringBuilder formatHighlight(int color, String format, Object... args) {
        final String message = String.format(format, args);
        SpannableStringBuilder builder = new SpannableStringBuilder(message);
        for (Object arg : args) {
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
            final String str = arg.toString();
            final int startIndex = message.indexOf(str);
            final int endIndex = startIndex + str.length();
            builder.setSpan(foregroundColorSpan, startIndex, endIndex, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

}
