package com.gmspace.app.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class ViewUtils {

    public static Bitmap toBitmap(View v) {
        final Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cvs = new Canvas(bitmap);
        v.draw(cvs);
        return bitmap;
    }
}
