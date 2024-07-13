package com.gmspace.app.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.gmspace.app.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FloatPointView extends FloatBaseView {
    private static final String FLOAT_TAG_VM = "point";

    public static void show(Activity context) {
        if (EasyFloat.isShow(FLOAT_TAG_VM)) {
            return;
        }
        EasyFloat.with(context.getApplication())
                .setLayout(R.layout.easyfloat_point_view, v -> {
                    final FloatPointView view = v.findViewById(R.id.float_view);
                    final EditText edx = view.findViewById(R.id.ed_sx);
                    final EditText edy = view.findViewById(R.id.ed_sy);
                    view.findViewById(R.id.btn_down).setOnClickListener(v1 -> {
                        view.sendPointer(Integer.parseInt(edx.getText().toString()), Integer.parseInt(edy.getText().toString()), MotionEvent.ACTION_DOWN);
                    });
                    view.findViewById(R.id.btn_up).setOnClickListener(v1 -> {
                        view.sendPointer(Integer.parseInt(edx.getText().toString()), Integer.parseInt(edy.getText().toString()), MotionEvent.ACTION_UP);
                    });
                    view.findViewById(R.id.btn_click).setOnClickListener(v1 -> {
                        view.sendPointer(Integer.parseInt(edx.getText().toString()), Integer.parseInt(edy.getText().toString()), MotionEvent.ACTION_DOWN);
                        view.sendPointer(Integer.parseInt(edx.getText().toString()), Integer.parseInt(edy.getText().toString()), MotionEvent.ACTION_UP);
                    });
                })
                .setShowPattern(ShowPattern.ALL_TIME)
                .setGravity(Gravity.END, 0, 0)
                .setLayoutChangedGravity(Gravity.END)
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                .setTag(FLOAT_TAG_VM)
                .setDragEnable(true)
                .hasEditText(true)
                .show();
    }

    public FloatPointView(@NonNull Context context) {
        super(context);
    }

    public FloatPointView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatPointView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    int onCreateViewLayoutId() {
        return R.layout.view_float_point;
    }

    @Override
    protected boolean canDrag() {
        return false;
    }


    private final ExecutorService mSingleThread = Executors.newSingleThreadExecutor();

    @SuppressLint("StaticFieldLeak")
    private void sendPointer(int x, int y, int action) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("Recycle")
            @Override
            protected Void doInBackground(Void... voids) {
                final MotionEvent obtain = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        action, x, y, 0);
                try {
                    GmSpaceUtils.getInstrumentation().sendPointerSync(obtain);
                }catch (SecurityException securityException) {
                    Log.d("iichen", securityException.getMessage());
                }
                return null;
            }
        }.executeOnExecutor(mSingleThread);
    }
}
