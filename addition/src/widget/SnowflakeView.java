package badtzmarupekkle.littlethings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SnowflakeView extends View {
    private static final int LIMIT_ADD_SNOWFLAKES = 1;
    private static final int LIMIT_SNOWFLAKES = 100;
    //private static final int LIMIT_X_DEVIATION=15;
    //private static final int LIMIT_X_DEVIATION_COUNT=10;
    private static final int SNOWFLAKE_DIAMETER = 31;
    private static final int SNOWFLAKE_RADIUS = 15;
    private static final int TICK_PIXEL = 1;

    private Bitmap colorGradient;
    private List<Snowflake> snowflakes;
    private Random random;

    public SnowflakeView(Context context) {
        super(context);
        init();
    }

    public SnowflakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnowflakeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator<Snowflake> itr = snowflakes.iterator();
        while (itr.hasNext()) {
            Snowflake s = itr.next();
            if (s.canTick()) {
                s.tick();
                s.draw(canvas);
            } else {
                itr.remove();
            }
        }
    }

    public void addSnowflake() {
        if (snowflakes.size() >= LIMIT_SNOWFLAKES) {
            return;
        }
        int count = random.nextInt(LIMIT_ADD_SNOWFLAKES) + 1;
        for (int i = 0; i < count; i++) {
            snowflakes.add(new Snowflake());
        }
    }

    public Random getRandom() {
        return random;
    }

    private void init() {
        random = new Random();
        snowflakes = new LinkedList<Snowflake>();

        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;

        colorGradient = Bitmap.createBitmap(1, screenHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(colorGradient);
        Paint paint = new Paint();
        int[] colors = {0xFFFF0000, 0xFFFF6600, 0xFFFFFF00, 0xFF00FF00, 0xFF0000FF, 0xFF660099, 0xFF9900CC};
        LinearGradient gradient = new LinearGradient(0, 0, 0, screenHeight, colors, null, TileMode.REPEAT);
        paint.setShader(gradient);
        canvas.drawPaint(paint);

        int x = random.nextInt(screenWidth - SNOWFLAKE_DIAMETER/*-LIMIT_X_DEVIATION*2*/);
        //x+=LIMIT_X_DEVIATION;
        snowflakes.add(new Snowflake(x));
    }

    private class Snowflake extends ShapeDrawable {
        //private boolean xDirection;

        private int x;
        //private int xDeviation;
        //private int xDeviationCount;
        private int y;

        public Snowflake() {
            super(new OvalShape());
            x = random.nextInt(getWidth() - SNOWFLAKE_DIAMETER/*-LIMIT_X_DEVIATION*2*/);
            //x+=LIMIT_X_DEVIATION;
            snowflakeInit();
        }

        public Snowflake(int x) {
            super(new OvalShape());
            this.x = x;
            snowflakeInit();
        }

        @Override
        public void draw(Canvas canvas) {
            int[] colors = {colorGradient.getPixel(0, y + SNOWFLAKE_RADIUS), Color.TRANSPARENT};
            RadialGradient rg = new RadialGradient(x/*+xDeviation*/ + SNOWFLAKE_RADIUS, y + SNOWFLAKE_RADIUS, SNOWFLAKE_RADIUS, colors, null, TileMode.CLAMP);
            getPaint().setShader(rg);
            canvas.drawOval(new RectF(getBounds()), getPaint());
        }

        public boolean canTick() {
            if (y + SNOWFLAKE_DIAMETER + TICK_PIXEL <= getHeight() && x + SNOWFLAKE_DIAMETER + TICK_PIXEL <= getWidth())
                return true;
            return false;
        }

        public void snowflakeInit() {
            //xDeviation=0;
            //xDeviationCount=random.nextInt(LIMIT_X_DEVIATION_COUNT);
            //xDirection=random.nextBoolean();
            y = 0;
            setBounds(x, y, x + SNOWFLAKE_DIAMETER, y + SNOWFLAKE_DIAMETER);
        }

        public void tick() {
            y++;
            /*if(xDirection) {
				if(xDeviation==LIMIT_X_DEVIATION) {
					if(xDeviationCount==0) {
						xDirection=false;
						xDeviationCount=random.nextInt(LIMIT_X_DEVIATION_COUNT);
					} else {
						xDeviationCount--;
					}
				} else {
					xDeviation++;
				}
			} else {
				if(xDeviation==-LIMIT_X_DEVIATION) {
					if(xDeviationCount==0) {
						xDirection=true;
						xDeviationCount=random.nextInt(LIMIT_X_DEVIATION_COUNT);
					} else {
						xDeviationCount--;
					}
				} else {
					xDeviation--;
				}
			}*/

            int direction = random.nextInt(4);
            if (direction == 3)
                x++;
            setBounds(x/*+xDeviation*/, y, x/*+xDeviation*/ + SNOWFLAKE_DIAMETER, y + SNOWFLAKE_DIAMETER);
        }
    }
}
