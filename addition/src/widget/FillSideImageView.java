package badtzmarupekkle.littlethings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FillSideImageView extends ImageView {
    private int displayHeight;

    public FillSideImageView(Context context) {
        this(context, null, 0);
    }

    public FillSideImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FillSideImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        displayHeight = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getDrawable() == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int height, width;
            int drawableHeight = getDrawable().getIntrinsicHeight();
            int drawableWidth = getDrawable().getIntrinsicWidth();

            width = MeasureSpec.getSize(widthMeasureSpec);
            height = width * drawableHeight / drawableWidth;

            if (height > displayHeight) {
                height = displayHeight;
                width = height * drawableWidth / drawableHeight;
            }

            setMeasuredDimension(width, height);
        }
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }
}
