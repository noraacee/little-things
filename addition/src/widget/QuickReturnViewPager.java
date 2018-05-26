package badtzmarupekkle.littlethings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class QuickReturnViewPager extends ViewPager {

    private static final int DURATION_RETURN_ANIMATION = 250;
    private static final int QUICK_RETURN_DISTANCE_DP = 30;

    private static enum ScrollState {
        OFF_SCREEN, ON_SCREEN, RETURNING_OFF, RETURNING_ON
    }

    private float downY;
    private float quickReturnDistance;
    float interpolation;

    private int height;
    private int touchSlop;

    private LinearInterpolator lInterpolator;
    private List<View> quickReturnDownViews;
    private ScrollState state;
    private View quickReturnUpView;

    public QuickReturnViewPager(Context context) {
        this(context, null);
    }

    public QuickReturnViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        downY = -1;
        interpolation = 0;
        lInterpolator = new LinearInterpolator();
        quickReturnDistance = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, QUICK_RETURN_DISTANCE_DP, getResources().getDisplayMetrics());
        quickReturnDownViews = new ArrayList<>();
        state = ScrollState.ON_SCREEN;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                height = getHeight();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float yDiff = event.getRawY() - downY;
                if(Math.abs(yDiff) > touchSlop) {
                    switch (state) {
                        case OFF_SCREEN:
                            if (yDiff > 0)
                                return onTouchEvent(event);
                            break;
                        case ON_SCREEN:
                            if (yDiff < 0)
                                return onTouchEvent(event);
                            break;
                        case RETURNING_OFF:
                        case RETURNING_ON:
                            return onTouchEvent(event);
                    }
                    downY = event.getRawY();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                returnViews();
                downY = -1f;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float yDiff = event.getRawY() - downY;
                if (downY != -1f) {
                    if (Math.abs(yDiff) > touchSlop) {
                        if(yDiff > 0) {
                            yDiff -= touchSlop;
                            switch(state) {
                                case ON_SCREEN:
                                    handled = false;
                                    break;
                                case OFF_SCREEN:
                                case RETURNING_ON:
                                    if(yDiff >= quickReturnDistance) {
                                        state = ScrollState.ON_SCREEN;

                                        ViewHelper.setTranslationY(quickReturnUpView, 0);
                                        for(int i = 0; i < quickReturnDownViews.size(); i++)
                                            ViewHelper.setTranslationY(quickReturnDownViews.get(i), 0);

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height);
                                        params.setMargins(0, 0, 0, 0);
                                        setLayoutParams(params);

                                        MotionEvent newEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, event.getX(), event.getY(), 0);
                                        return super.dispatchTouchEvent(newEvent);
                                    } else {
                                        state = ScrollState.RETURNING_ON;

                                        interpolation = lInterpolator.getInterpolation(yDiff / quickReturnDistance);
                                        float translationY = -quickReturnUpView. getHeight() + interpolation * quickReturnUpView.getHeight();
                                        ViewHelper.setTranslationY(quickReturnUpView, translationY);
                                        for(int i = 0; i < quickReturnDownViews.size(); i++) {
                                            View quickReturnDownView = quickReturnDownViews.get(i);
                                            float translationYDown = quickReturnDownView.getHeight() - interpolation * quickReturnDownView.getHeight();
                                            ViewHelper.setTranslationY(quickReturnDownView, translationYDown);
                                        }

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height - (int) translationY);
                                        params.setMargins(0, (int) translationY, 0, 0);
                                        setLayoutParams(params);
                                    }
                                    handled = true;
                                    break;
                            }
                        } else {
                            yDiff += touchSlop;
                            switch(state) {
                                case OFF_SCREEN:
                                    handled = false;
                                    break;
                                case ON_SCREEN:
                                case RETURNING_OFF:
                                    if( yDiff <= -quickReturnDistance) {
                                        state = ScrollState.OFF_SCREEN;

                                        ViewHelper.setTranslationY(quickReturnUpView, -quickReturnUpView.getHeight());
                                        for(int i = 0; i < quickReturnDownViews.size(); i++)
                                            ViewHelper.setTranslationY(quickReturnDownViews.get(i), quickReturnDownViews.get(i).getHeight());

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height + quickReturnUpView.getHeight());
                                        params.setMargins(0, -quickReturnUpView.getHeight(), 0, 0);
                                        setLayoutParams(params);

                                        MotionEvent newEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, event.getX(), event.getY(), 0);
                                        return super.dispatchTouchEvent(newEvent);
                                    } else {
                                        state = ScrollState.RETURNING_OFF;

                                        interpolation = lInterpolator.getInterpolation(-yDiff / quickReturnDistance);
                                        float translationY = -interpolation * quickReturnUpView.getHeight();
                                        ViewHelper.setTranslationY(quickReturnUpView, translationY);
                                        for(int i = 0; i < quickReturnDownViews.size(); i++) {
                                            View quickReturnDownView = quickReturnDownViews.get(i);
                                            float translationYDown = interpolation * quickReturnDownView.getHeight();
                                            ViewHelper.setTranslationY(quickReturnDownView, translationYDown);
                                        }

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height - (int) translationY);
                                        params.setMargins(0, (int) translationY, 0, 0);
                                        setLayoutParams(params);
                                    }
                                    handled = true;
                                    break;
                            }
                        }
                    }
                } else {
                    downY = event.getRawY();
                }
                break;
        }

        if (!handled)
            handled = super.onTouchEvent(event);

        return handled;
    }

    public void addQuickReturnDownView(View quickReturnDownView) {
        if(quickReturnDownView != null)
            quickReturnDownViews.add(quickReturnDownView);
    }

    public void setQuickReturnUpView(View quickReturnUpView) {
        this.quickReturnUpView = quickReturnUpView;
    }

    private void returnViews() {
        switch(state) {
            case RETURNING_OFF:
                Animation animationOn = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        float translationY = -interpolation * quickReturnUpView.getHeight();
                        ViewHelper.setTranslationY(quickReturnUpView, translationY - translationY * interpolatedTime);
                        for(int i = 0; i < quickReturnDownViews.size(); i++) {
                            View quickReturnDownView = quickReturnDownViews.get(i);
                            float translationYDown = interpolation * quickReturnDownView.getHeight();
                            ViewHelper.setTranslationY(quickReturnDownView, translationYDown - translationYDown * interpolatedTime);
                        }

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height - (int) (translationY - translationY * interpolatedTime));
                        params.setMargins(0, (int) (translationY - translationY * interpolatedTime), 0, 0);
                        setLayoutParams(params);
                    }
                };

                animationOn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        state = ScrollState.ON_SCREEN;
                        ViewHelper.setTranslationY(quickReturnUpView, 0);
                        for(int i = 0; i < quickReturnDownViews.size(); i++)
                            ViewHelper.setTranslationY(quickReturnDownViews.get(i), 0);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height);
                        params.setMargins(0, 0, 0, 0);
                        setLayoutParams(params);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                animationOn.setDuration(DURATION_RETURN_ANIMATION);
                startAnimation(animationOn);
                break;
            case RETURNING_ON:
                Animation animationOff = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        float translationY = -quickReturnUpView. getHeight() + interpolation * quickReturnUpView.getHeight();
                        ViewHelper.setTranslationY(quickReturnUpView, translationY - (quickReturnUpView.getHeight() + translationY) * interpolatedTime);
                        for(int i = 0; i < quickReturnDownViews.size(); i++) {
                            View quickReturnDownView = quickReturnDownViews.get(i);
                            float translationYDown = quickReturnDownView.getHeight() - interpolation * quickReturnDownView.getHeight();
                            ViewHelper.setTranslationY(quickReturnDownView, translationYDown + (quickReturnDownView.getHeight() - translationYDown) * interpolatedTime);
                        }

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height - (int) (translationY - (quickReturnUpView.getHeight() + translationY) * interpolatedTime));
                        params.setMargins(0, (int) (translationY - (quickReturnUpView.getHeight() + translationY) * interpolatedTime), 0, 0);
                        setLayoutParams(params);
                    }
                };

                animationOff.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        state = ScrollState.OFF_SCREEN;
                        ViewHelper.setTranslationY(quickReturnUpView, -quickReturnUpView.getHeight());
                        for(int i = 0; i < quickReturnDownViews.size(); i++)
                            ViewHelper.setTranslationY(quickReturnDownViews.get(i), quickReturnDownViews.get(i).getHeight());

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getWidth(), height + quickReturnUpView.getHeight());
                        params.setMargins(0, -quickReturnUpView.getHeight(), 0, 0);
                        setLayoutParams(params);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                animationOff.setDuration(DURATION_RETURN_ANIMATION);
                startAnimation(animationOff);
                break;
        }
    }
}
