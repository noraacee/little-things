package badtzmarupekkle.littlethings.interf;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnMultiTouchListener implements OnTouchListener {
    public static enum Mode {
        SINGLE_TOUCH, MULTI_TOUCH;
    }

    private final Mode mode;

    private final GestureDetector gDetector;
    private final ScaleGestureDetector sgDetector;

    public OnMultiTouchListener(Context context, Mode mode) {
        this.mode = mode;
        gDetector = new GestureDetector(context, new GestureListener());
        sgDetector = new ScaleGestureDetector();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (mode) {
            case SINGLE_TOUCH:
                return gDetector.onTouchEvent(event);
            case MULTI_TOUCH:
                return sgDetector.onTouchEvent(event);
        }
        return false;
    }

    public boolean onPinchIn() {
        return false;
    }

    public boolean onPinchOut() {
        return false;
    }

    public boolean onSwipeDown() {
        return false;
    }

    public boolean onSwipeLeft() {
        return false;
    }

    public boolean onSwipeRight() {
        return false;
    }

    public boolean onSwipeUp() {
        return false;
    }

    private class GestureListener extends SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0)
                    return onSwipeRight();
                else
                    return onSwipeLeft();
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0)
                    return onSwipeDown();
                else
                    return onSwipeUp();
            }

            return false;
        }
    }

    private class ScaleGestureDetector {
        private static final int SCALE_THRESHOLD = 50;
        private static final int SCALE_VELOCITY_THRESHOLD = 100;

        private boolean inProgress;

        private float span;
        private float spanPrevious;

        private long timeEnd;
        private long timeStart;

        public ScaleGestureDetector() {
            reset();
        }

        public boolean onTouchEvent(MotionEvent e) {
            boolean handled = inProgress;

            int action = e.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN)
                reset();

            if (!inProgress) {
                switch (action) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (e.getPointerCount() == 2) {
                            inProgress = true;
                            timeStart = System.currentTimeMillis();

                            float spanX = Math.abs(e.getX() - e.getX(1));
                            float spanY = Math.abs(e.getY() - e.getY(1));
                            spanPrevious = (float) Math.sqrt(spanX * spanX + spanY * spanY);
                            handled = true;
                        }
                        break;
                }
            } else {
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        if (e.getPointerCount() == 2) {
                            float spanX = Math.abs(e.getX() - e.getX(1));
                            float spanY = Math.abs(e.getY() - e.getY(1));
                            span = (float) Math.sqrt(spanX * spanX + spanY * spanY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        timeEnd = System.currentTimeMillis();

                        handled = onScale(span - spanPrevious, (span - spanPrevious) / (timeEnd - timeStart) * 1000);
                        reset();
                        break;
                }
            }
            return handled;
        }

        public boolean onScale(float diffSpan, float velocity) {
            if (Math.abs(diffSpan) > SCALE_THRESHOLD && Math.abs(velocity) > SCALE_VELOCITY_THRESHOLD) {
                if (diffSpan > 0)
                    return onPinchOut();
                else
                    return onPinchIn();
            }
            return false;
        }

        private void reset() {
            inProgress = false;

            span = 0;
            spanPrevious = 0;

            timeEnd = 0;
            timeStart = 0;
        }
    }
}
