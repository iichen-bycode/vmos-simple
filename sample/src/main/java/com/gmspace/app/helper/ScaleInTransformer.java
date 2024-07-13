package com.gmspace.app.helper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class ScaleInTransformer implements ViewPager2.PageTransformer {
    public static float DEFAULT_MIN_SCALE = 0.95F;
    private static float DEFAULT_CENTER = 0.5f;
    private float mMinScale = DEFAULT_MIN_SCALE;

    @Override
    public void transformPage(@NonNull View page, float position) {
        float pageWidth = page.getWidth();
        float pageHeight = page.getHeight();
        page.setPivotX((pageWidth / 2));
        page.setPivotY(pageHeight / 2);
        if (position < -1) {
            page.setScaleX(mMinScale);
            page.setScaleY(mMinScale);
            page.setPivotY(pageWidth / 2);
        } else if (position <= 1) {
            if (position < 0) {
                float scaleFactor = (1 + position) * (1 - mMinScale) + mMinScale;
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setPivotX(pageWidth);
            } else {
                float scaleFactor = (1 - position) * (1 - mMinScale) + mMinScale;
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setPivotX(pageWidth * (1 - position) * DEFAULT_MIN_SCALE);
            }
        } else {
            page.setScaleX(0f);
            page.setScaleY(mMinScale);
            page.setScaleY(mMinScale);
        }
    }
}
