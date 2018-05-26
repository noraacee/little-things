package badtzmarupekkle.littlethings.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.ListView;

import badtzmarupekkle.littlethings.interf.Refresher;

public class RefreshListView extends ListView implements OnRefreshListener, Refresher {

    private static final float ACCELERATE_INTERPOLATOR_FACTOR = 1.5f;

    private static final int REFRESH_TRIGGER_DISTANCE_DP = 150;

    private boolean refreshing;

    private float downY;
    private float refreshTriggerDistance;

    private int touchSlop;

    private AccelerateInterpolator aInterpolator;
    private OnRefreshListener rListener;
    private RefreshViewPagerTabs refreshViewPagerTabs;

    public RefreshListView(Context context) {
        this(context, null, 0);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        aInterpolator = new AccelerateInterpolator(ACCELERATE_INTERPOLATOR_FACTOR);
        downY = -1f;
        refreshing = false;
        refreshViewPagerTabs = null;
        refreshTriggerDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, REFRESH_TRIGGER_DISTANCE_DP, getResources().getDisplayMetrics());
        rListener = null;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (atListStart())
                    return true;
                else
                    downY = event.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                downY = -1f;
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void onRefresh() {
        if (rListener != null)
            rListener.onRefresh();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float eventY = event.getY();
                float yDiff = eventY - downY;

                if (downY != -1f && !refreshing && atListStart()) {
                    if (yDiff > touchSlop) {
                        if (yDiff > refreshTriggerDistance) {
                            setDragPercentage(1f);
                            startRefresh();
                        } else if (yDiff >= 0f) {
                            setDragPercentage(aInterpolator.getInterpolation(yDiff / refreshTriggerDistance));
                        }

                        handled = true;
                    }
                } else if (!refreshing && !atListStart()) {
                    downY = event.getY();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setDragPercentage(0f);
                downY = -1f;
                break;
        }

        if (!handled)
            handled = super.onTouchEvent(event);

        return handled;
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener rListener) {
        this.rListener = rListener;
    }

    @Override
    public void setRefreshViewPagerTabs(RefreshViewPagerTabs refreshViewPagerTabs) {
        this.refreshViewPagerTabs = refreshViewPagerTabs;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        if (refreshViewPagerTabs != null)
            refreshViewPagerTabs.setRefreshing(refreshing);
    }

    private boolean atListStart() {
        return getChildCount() == 0 || getChildAt(0).getTop() >= 0;
    }

    private void setDragPercentage(float dragPercentage) {
        if (refreshViewPagerTabs != null)
            refreshViewPagerTabs.setDragPercentage(dragPercentage);

    }

    private void startRefresh() {
        refreshing = true;
        if (refreshViewPagerTabs != null)
            refreshViewPagerTabs.setRefreshing(true);
        onRefresh();
    }
}
