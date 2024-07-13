package com.gmspace.app.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    public static CharSequence getClipboardText(Context context) {
        try {
            ClipboardManager cbMgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cbMgr.hasPrimaryClip()) {
                final ClipData primaryClip = cbMgr.getPrimaryClip();
                if (primaryClip != null && primaryClip.getItemCount() > 0) {
                    return primaryClip.getItemAt(0).getText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getClipboardString(Context context) {
        final CharSequence text = getClipboardText(context);
        return text == null ? null : text.toString();
    }
}
