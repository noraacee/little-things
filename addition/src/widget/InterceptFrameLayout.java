package badtzmarupekkle.littlethings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class InterceptFrameLayout extends FrameLayout {
    private OnTouchListener tListener;

    public InterceptFrameLayout(Context context) {
        super(context);
        tListener = null;
    }

    public InterceptFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        tListener = null;
    }

    public InterceptFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        tListener = null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (tListener != null) {
            if (tListener.onTouch(this, ev))
                return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setOnTouchListener(OnTouchListener tListener) {
        this.tListener = tListener;
    }
}
