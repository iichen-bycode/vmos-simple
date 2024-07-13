package com.gmspace.app.fragments;

import static java.lang.Math.abs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmspace.sdk.proxy.GmSpaceUtils;
import com.gmspace.app.R;
import com.gmspace.app.adapters.RunningTaskCardAdapter;
import com.gmspace.app.bean.RunningInfo;
import com.gmspace.app.databinding.FragmentRunningTaskBinding;
import com.gmspace.app.helper.SimpleItemTouchCallback;
import com.gmspace.app.helper.ItemTouchStatus;
import com.gmspace.app.helper.ScrollSmoothHelper;

import java.util.ArrayList;
import java.util.List;

public class RunningTaskFragment extends DialogFragment {
    private FragmentRunningTaskBinding binding;
    private List<RunningInfo> runningInfos = new ArrayList<>();

    public RunningTaskFragment setRunningInfos(List<RunningInfo> runningInfos) {
        this.runningInfos = runningInfos;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(AppCompatDialogFragment.STYLE_NO_FRAME, R.style.Theme_App_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = FragmentRunningTaskBinding.inflate(inflater);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final int heightPixels = getResources().getDisplayMetrics().heightPixels;
        final Window window = getDialog().getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, heightPixels);
//        window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        bindRunningTaskViews(runningInfos);
    }

    private void bindRunningTaskViews(List<RunningInfo> result) {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                dismissAllowingStateLoss();
                return true;
            }
        });
        binding.rvApps.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvApps.setLayoutManager(layoutManager);

        RunningTaskCardAdapter adapter = new RunningTaskCardAdapter(result);
        adapter.setOnClickPlaceholderListener(position -> {
            dismissAllowingStateLoss();
        });
        binding.rvApps.setAdapter(adapter);

        //一次性滑动多页
        ScrollSmoothHelper scrollSmoothHelper = new ScrollSmoothHelper();
        scrollSmoothHelper.attachToRecyclerView(binding.rvApps);

        //实现动画效果
        binding.rvApps.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private float MIN_SCALE = 0.95f;
            private float MAX_SCALE = 1f;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int childCount = recyclerView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = recyclerView.getChildAt(i);
                    int left = child.getLeft();
                    int paddingStart = recyclerView.getPaddingStart();
                    float b1 = Math.min(1, abs(left - paddingStart) * 1f / child.getWidth());
                    float scale = MAX_SCALE - b1 * (MAX_SCALE - MIN_SCALE);
                    child.setScaleY(scale);
                }
            }
        });


        final SimpleItemTouchCallback itemTouchCallback = new SimpleItemTouchCallback(new ItemTouchStatus() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public boolean onItemRemove(int position) {
                final RunningInfo item = result.get(position);
                adapter.getData().remove(position);
                adapter.notifyItemRemoved(position);
                GmSpaceUtils.killApplication(item.getPackageName());
                if (adapter.getData().isEmpty()){
                    dismissAllowingStateLoss();
                }
                return true;
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvApps);
    }

    public void show(@NonNull FragmentManager manager) {
        super.show(manager, getClass().getName());
    }
}
