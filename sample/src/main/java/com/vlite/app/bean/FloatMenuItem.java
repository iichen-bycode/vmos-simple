package com.vlite.app.bean;

import android.view.View;

public class FloatMenuItem {
    private int icon;
    private String name;
    private View.OnClickListener clickListener;

    public FloatMenuItem(int icon, String name, View.OnClickListener clickListener) {
        this.icon = icon;
        this.name = name;
        this.clickListener = clickListener;
    }

    public FloatMenuItem() {
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public View.OnClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
