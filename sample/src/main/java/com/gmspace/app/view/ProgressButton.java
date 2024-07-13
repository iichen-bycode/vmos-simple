package com.gmspace.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gmspace.app.R;
import com.gmspace.app.databinding.ViewProgressButtonBinding;

public class ProgressButton extends ConstraintLayout {
    public final static int STYLE_TYPE_PRIMARY = 0;
    public final static int STYLE_TYPE_INFO = 1;

    private final ViewProgressButtonBinding binding;
    private final GradientDrawable gradientDrawable = new GradientDrawable();

    private boolean plain;
    private int styleType;

    public ProgressButton(@NonNull Context context) {
        this(context, null);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgressButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.binding = ViewProgressButtonBinding.inflate(LayoutInflater.from(context), this);
        final int padding = getResources().getDimensionPixelSize(R.dimen.padding_progress_button);
        setPadding(padding, padding, padding, padding);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);
        final String buttonText = a.getString(R.styleable.ProgressButton_android_text);
        this.styleType = a.getInt(R.styleable.ProgressButton_styleType, STYLE_TYPE_PRIMARY);
        this.plain = a.getBoolean(R.styleable.ProgressButton_plain, false);
        a.recycle();

        gradientDrawable.setCornerRadius(getResources().getDimensionPixelSize(R.dimen.radius_progress_button));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        applyStyle();
        setText(buttonText);
        setBackground(gradientDrawable);
    }

    public void setStyleType(int styleType, boolean plain) {
        this.styleType = styleType;
        this.plain = plain;
        applyStyle();
    }

    public void applyStyle() {
        int color;
        int textColor;
        int plainTextColor;
        if (STYLE_TYPE_INFO == styleType) {
            color = getResources().getColor(R.color.colorBackgroundInfo);
            textColor = getResources().getColor(R.color.colorBackgroundInfo);
            plainTextColor = getResources().getColor(R.color.colorBackgroundInfo);
        }else{
            color = getResources().getColor(R.color.colorPrimary);
            textColor = Color.WHITE;
            plainTextColor = getResources().getColor(R.color.colorPrimary);
        }
        if (plain) {
            gradientDrawable.setStroke(getResources().getDimensionPixelSize(R.dimen.stroke_width_progress_button),color);
            gradientDrawable.setColor(Color.WHITE);
            binding.tvMainText.setTextColor(plainTextColor);
        } else {
            gradientDrawable.setStroke(0, 0);
            gradientDrawable.setColor(color);
            binding.tvMainText.setTextColor(textColor);
        }
    }

    public void setShowProgress(boolean showProgress) {
        binding.progress.setVisibility(showProgress ? VISIBLE : INVISIBLE);
    }

    public void setProgressText(boolean showProgress, CharSequence text) {
        this.setShowProgress(showProgress);
        binding.tvMainText.setText(text);
    }

    public void setProgressText(boolean showProgress, @StringRes int resid) {
        this.setShowProgress(showProgress);
        binding.tvMainText.setText(resid);
    }


    public void setText(CharSequence text) {
        binding.tvMainText.setText(text);
    }

}
