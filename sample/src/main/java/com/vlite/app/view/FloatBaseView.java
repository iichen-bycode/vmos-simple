package com.vlite.app.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.vlite.app.R;

public abstract class FloatBaseView extends ConstraintLayout implements TouchProxy.OnTouchEventListener {
    private final TouchProxy mTouchProxy = new TouchProxy(this);

    private final int mMaxWidth;
    private final int mMaxHeight;
    private final int mMinY;

    private OnMoveListener moveListener;

    public static FloatBaseView attach(View parent, int viewId, PointF point, FloatViewFactory factory) {
        FloatBaseView findView = parent.findViewById(viewId);
        if (findView != null) {
            return findView;
        }
        FloatBaseView floatView = factory.onCreateFloatView(parent.getContext());
        if (parent instanceof ViewGroup) {
            floatView.setId(viewId);
            floatView.setX(point.x);
            floatView.setY(Math.max(getStatusBarHeight(parent.getContext()), point.y));
            ((ViewGroup) parent).addView(floatView, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        }
        return floatView;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelSize(resId);
        }
        return result;
    }

    public FloatBaseView(@NonNull Context context) {
        this(context, null);
    }

    public FloatBaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatBaseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Point screenSize = getScreenSize();
        mMaxWidth = screenSize.x;
        mMaxHeight = screenSize.y;
        mMinY = 0;//getStatusBarHeight(context)
        setY(mMinY);

        onCreateView(context);
    }

    protected void onCreateView(Context context) {
        View.inflate(context, onCreateViewLayoutId(), this);
        View closeView = findViewById(R.id.close);
        if (closeView != null) {
            closeView.setOnClickListener(v -> {
                detachView();
            });
        }
    }

    abstract int onCreateViewLayoutId();

    @Override
    public void onMove(int x, int y, int dx, int dy) {
        if (!canDrag()) {
            return;
        }
        float nx = getX() + dx;
        float ny = getY() + dy;

        float targetX = Math.min(Math.max(nx, 0f), (float) mMaxWidth - getWidth());
        float targetY = Math.min(Math.max(ny, (float) mMinY), (float) mMaxHeight - getHeight());
        //Log.d("------->", "${targetX}--->${targetY}-->${mMaxWidth}--${nx}-->${width}")
        if (moveListener == null) {
            setX(targetX);
            setY(targetY);
        } else {
            moveListener.onMove((int) targetX, (int) targetY, dx, dy);
        }
    }

    protected boolean canDrag() {
        return true;
    }

    private Point getScreenSize() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point;
    }

    private interface OnMoveListener {
        void onMove(int x, int y, int dx, int dy);
    }

    void detachView() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchProxy.onTouchEvent(this, event)) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void onUp(int x, int y) {
        if (!canDrag()) {
        }
    }

    @Override
    public void onDown(int x, int y) {

    }

    public interface FloatViewFactory {
        FloatBaseView onCreateFloatView(Context context);
    }
}
