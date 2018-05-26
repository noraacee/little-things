package badtzmarupekkle.littlethings.widget;

import android.content.Context;
import android.content.res.Resources;
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
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import badtzmarupekkles.littlethings.R;

public class SnowflakeView extends View {
    private static final int ADD_SNOWFLAKE = 1;
    private static final int ADD_SNOWFLAKE_BASE_DELAY = 100;
    private static final int LIMIT_ADD_SNOWFLAKE_DELAY = 14;
    private static final int TICK_SNOWFLAKES = 0;
    private static final int TICK_SNOWFLAKES_DELAY = 10;

    private static final int LIMIT_ADD_SNOWFLAKES = 1;
    private static final int LIMIT_SNOWFLAKES = 100;
    private static final int SNOWFLAKE_DIAMETER = 31;
    private static final int SNOWFLAKE_RADIUS = 15;
    private static final int TICK_PIXEL = 1;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TICK_SNOWFLAKES:
                    invalidate();
                    handler.sendEmptyMessageDelayed(TICK_SNOWFLAKES, TICK_SNOWFLAKES_DELAY);
                    break;
                case ADD_SNOWFLAKE:
                    addSnowflake();
                    handler.sendEmptyMessageDelayed(ADD_SNOWFLAKE, (random.nextInt(LIMIT_ADD_SNOWFLAKE_DELAY) + 1) * ADD_SNOWFLAKE_BASE_DELAY);
                    break;
            }
        }
    };

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

    private void addSnowflake() {
        if (snowflakes.size() >= LIMIT_SNOWFLAKES) {
            return;
        }
        int count = random.nextInt(LIMIT_ADD_SNOWFLAKES) + 1;
        for (int i = 0; i < count; i++) {
            snowflakes.add(new Snowflake());
        }
    }

    private void init() {
        random = new Random();
        snowflakes = new LinkedList<>();

        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;

        colorGradient = Bitmap.createBitmap(1, screenHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(colorGradient);
        Paint paint = new Paint();
        Resources r = getResources();
        int[] colors = {r.getColor(R.color.pink_500), r.getColor(R.color.red_500), r.getColor(R.color.orange_500), r.getColor(R.color.deep_orange_500), r.getColor(R.color.yellow_500),
                r.getColor(R.color.amber_500), r.getColor(R.color.green_500), r.getColor(R.color.blue_500), r.getColor(R.color.indigo_500), r.getColor(R.color.purple_500),
                r.getColor(R.color.deep_purple_500)};
        LinearGradient gradient = new LinearGradient(0, 0, 0, screenHeight, colors, null, TileMode.REPEAT);
        paint.setShader(gradient);
        canvas.drawPaint(paint);

        int x = random.nextInt(screenWidth - SNOWFLAKE_DIAMETER);
        snowflakes.add(new Snowflake(x));
        handler.sendEmptyMessageDelayed(ADD_SNOWFLAKE, (random.nextInt(LIMIT_ADD_SNOWFLAKE_DELAY) + 1) * ADD_SNOWFLAKE_BASE_DELAY);
        handler.sendEmptyMessageDelayed(TICK_SNOWFLAKES, TICK_SNOWFLAKES_DELAY);
    }

    private class Snowflake extends ShapeDrawable {

        private int x;
        private int y;

        public Snowflake() {
            super(new OvalShape());
            x = random.nextInt(getWidth() - SNOWFLAKE_DIAMETER);
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
            RadialGradient rg = new RadialGradient(x + SNOWFLAKE_RADIUS, y + SNOWFLAKE_RADIUS, SNOWFLAKE_RADIUS, colors, null, TileMode.CLAMP);
            getPaint().setShader(rg);
            canvas.drawOval(new RectF(getBounds()), getPaint());
        }

        public boolean canTick() {
            return y + SNOWFLAKE_DIAMETER + TICK_PIXEL <= getHeight() && x + SNOWFLAKE_DIAMETER + TICK_PIXEL <= getWidth();
        }

        public void snowflakeInit() {
            y = 0;
            setBounds(x, y, x + SNOWFLAKE_DIAMETER, y + SNOWFLAKE_DIAMETER);
        }

        public void tick() {
            y++;

            int direction = random.nextInt(4);
            if (direction == 3)
                x++;
            setBounds(x, y, x + SNOWFLAKE_DIAMETER, y + SNOWFLAKE_DIAMETER);
        }
    }
}
