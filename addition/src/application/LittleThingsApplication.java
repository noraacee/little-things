package badtzmarupekkle.littlethings.application;

import android.app.Application;

public class LittleThingsApplication extends Application {
    private static boolean visible = false;

    public static boolean isVisible() {
        return visible;
    }

    public static void onPause() {
        visible = false;
    }

    public static void onResume() {
        visible = true;
    }
}
