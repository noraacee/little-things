package badtzmarupekkle.littlethings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import badtzmarupekkles.littlethings.R;

@SuppressLint("HandlerLeak")
public class RefreshViewPagerTabs extends LinearLayout {
    public interface IconTabProvider {
        public int getTabIconResId(int position);
    }

    private static final float REFRESH_POSITION_INCREMENT = 5f;

    private static final int DEF_BACKGROUND_COLOR = 0xFFFFFFFF;
    private static final int DEF_DIVIDER_WIDTH = 1;
    private static final int DEF_INDICATOR_HEIGHT = 8;
    //private static final int DEF_SCROLL_OFFSET = 52;
    private static final int DEF_TAB_PADDING = 16;
    private static final int DELAY_MESSAGE = 20;

    private final Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (refreshPosition + REFRESH_POSITION_INCREMENT <= getWidth())
                refreshPosition += REFRESH_POSITION_INCREMENT;
            else
                refreshPosition = 0f;
            invalidate();

            refreshHandler.sendEmptyMessageDelayed(0, DELAY_MESSAGE);
        }
    };

    private float currentPositionOffset;
    private float dragPercentage;
    private float refreshPosition;

    private int backgroundColor;
    private int currentPosition;
    private int indicatorHeight;
    private int refreshCount;
    private int tabCount;
    private int tabPadding;


    private FrameLayout.LayoutParams iconLayoutParams;
    private LinearLayout.LayoutParams tabLayoutParams;
    private ViewPager viewPager;

    private OnPageChangeListener opcListener;
    private Paint dividerPaint;
    private Paint indicatorPaint;
    private PageListener pListener;

    public RefreshViewPagerTabs(Context context) {
        this(context, null);
    }

    public RefreshViewPagerTabs(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        setOrientation(LinearLayout.HORIZONTAL);

        opcListener = null;
        pListener = new PageListener();
        viewPager = null;

        backgroundColor = DEF_BACKGROUND_COLOR;
        currentPositionOffset = 0;
        dragPercentage = 0;
        refreshCount = 0;
        refreshPosition = 0;
        tabCount = 0;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_DIVIDER_WIDTH, dm);
        indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_INDICATOR_HEIGHT, dm);
        //int scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_SCROLL_OFFSET, dm);
        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_TAB_PADDING, dm);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshViewPagerTabs);

        indicatorHeight = a.getDimensionPixelSize(R.styleable.RefreshViewPagerTabs_rvptIndicatorHeight, indicatorHeight);
        //scrollOffset = a.getDimensionPixelSize(R.styleable.RefreshViewPagerTabs_rvptScrollOffset, scrollOffset);
        tabPadding = a.getDimensionPixelSize(R.styleable.RefreshViewPagerTabs_rvptTabPadding, tabPadding);

        a.recycle();

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setColor(Color.WHITE);
        dividerPaint.setStrokeWidth(dividerWidth);

        indicatorPaint = new Paint();
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStyle(Style.FILL);

        iconLayoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iconLayoutParams.gravity = Gravity.CENTER;
    }

    @Override
    public void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        drawOnTop(canvas);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void notifyDataSetChanged() {
        if (!(viewPager.getAdapter() instanceof IconTabProvider))
            return;

        removeAllViews();
        tabCount = viewPager.getAdapter().getCount();
        tabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f / tabCount);

        for (int i = 0; i < tabCount; i++) {
            addTab(i, ((IconTabProvider) viewPager.getAdapter()).getTabIconResId(i));
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                currentPosition = viewPager.getCurrentItem();
            }
        });

    }

    public void setDragPercentage(float dragPercentage) {
        this.dragPercentage = dragPercentage;
        invalidate();
    }

    public synchronized void setRefreshing(boolean refreshing) {
        if (refreshing) {
            refreshCount++;
            if (refreshCount == 1)
                refreshHandler.sendEmptyMessage(0);
        } else {
            refreshCount--;
            if (refreshCount <= 0) {
                refreshHandler.removeMessages(0);
                refreshPosition = 0f;
                invalidate();
                refreshCount = 0;
            }
        }
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;

        if (viewPager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");

        viewPager.setOnPageChangeListener(pListener);

        notifyDataSetChanged();
    }

    private void addTab(final int position, int resId) {
        ImageView icon = new ImageView(getContext());
        icon.setBackgroundColor(backgroundColor);
        icon.setImageDrawable(getResources().getDrawable(resId));

        FrameLayout tab = new FrameLayout(getContext());
        tab.setBackgroundColor(backgroundColor);
        tab.setFocusable(true);
        tab.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(position);
            }
        });

        tab.addView(icon, iconLayoutParams);
        addView(tab, position, tabLayoutParams);
    }

    private synchronized void drawOnTop(Canvas canvas) {
        if (isInEditMode() || tabCount == 0) {
            return;
        }

        int height = getHeight();
        int width = getWidth();

        for (int i = 0; i < tabCount - 1; i++) {
            View tab = getChildAt(i);
            canvas.drawLine(tab.getRight(), 0, tab.getRight(), height, dividerPaint);
        }

        if (refreshCount > 0) {
            float lineRight = getChildAt(0).getRight();
            if (refreshPosition + lineRight <= width) {
                canvas.drawRect(refreshPosition, height - indicatorHeight, lineRight + refreshPosition, height, indicatorPaint);
            } else {
                float overflow = refreshPosition + lineRight - width;
                canvas.drawRect(refreshPosition, height - indicatorHeight, width, height, indicatorPaint);
                canvas.drawRect(0, height - indicatorHeight, overflow, height, indicatorPaint);
            }
        } else if (dragPercentage == 0f) {
            View currentTab = getChildAt(currentPosition);
            float lineLeft = currentTab.getLeft();
            float lineRight = currentTab.getRight();

            if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
                View nextTab = getChildAt(currentPosition + 1);
                float nextTabLeft = nextTab.getLeft();
                float nextTabRight = nextTab.getRight();

                lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
                lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
            }

            canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, indicatorPaint);
        } else {
            float middle = getWidth() / 2;
            float offset = middle * dragPercentage;

            canvas.drawRect(middle - offset, height - indicatorHeight, middle + offset, height, indicatorPaint);
        }
    }

    private class PageListener implements OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosition = position;
            currentPositionOffset = positionOffset;

            invalidate();

            if (opcListener != null) {
                opcListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (opcListener != null) {
                opcListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (opcListener != null) {
                opcListener.onPageSelected(position);
            }
        }

    }
}
