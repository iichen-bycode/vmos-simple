package com.gmspace.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gmspace.app.R;
import com.gmspace.app.databinding.ViewCheckboxMenuBinding;

public class CheckBoxMenuView extends ConstraintLayout implements Checkable {
    private final ViewCheckboxMenuBinding mBinding;

    public CheckBoxMenuView(@NonNull Context context) {
        this(context, null);
    }

    public CheckBoxMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckBoxMenuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBinding = ViewCheckboxMenuBinding.inflate(LayoutInflater.from(context), this);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxMenuView);
            final String title = a.getString(R.styleable.CheckBoxMenuView_android_title);
            final String subtitle = a.getString(R.styleable.CheckBoxMenuView_android_subtitle);
            final boolean checked = a.getBoolean(R.styleable.CheckBoxMenuView_android_checked, false);
            a.recycle();

            setChecked(checked);
            mBinding.tvMenuTitle.setText(title);
            mBinding.tvMenuSubtitle.setText(subtitle);
        }
        setOnClickListener((v) -> toggle());
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mBinding.cbMenu.setOnCheckedChangeListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled? 1f: 0.5f);
    }

    @Override
    public void setChecked(boolean checked) {
        mBinding.cbMenu.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return mBinding.cbMenu.isChecked();
    }

    @Override
    public void toggle() {
        mBinding.cbMenu.toggle();
    }
}
