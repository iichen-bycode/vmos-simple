package com.vlite.app.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            onDialogShow(dialog);
        });
        return dialog;
    }

    protected void onDialogShow(Dialog dialog) {
        final int defaultPeekHeight = getDefaultPeekHeight();
        setBehaviorPeekHeight(dialog, defaultPeekHeight);
    }

    protected int getDefaultPeekHeight() {
        return (int) (getResources().getDisplayMetrics().heightPixels * 0.65f);
    }

    protected void setBehaviorPeekHeight(Dialog dialog, int peekHeight) {
        try {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            final BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(peekHeight);
        } catch (Exception ignored) {
        }
    }

    public void show(@NonNull FragmentManager manager) {
        super.show(manager, getClass().getName());
    }

}
