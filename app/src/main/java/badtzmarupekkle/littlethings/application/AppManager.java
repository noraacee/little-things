package badtzmarupekkle.littlethings.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import badtzmarupekkles.littlethings.R;

public class AppManager {
    private static final String PROPERTY_COLOR = "Color";
    private static final String PROPERTY_WRITER = "Writer";
    private static final String SECRET = "PektzMaru";
    private static final String USER = "USER";

    private static boolean is24Hours;

    private static int color;

    private static Context context;

    public static boolean checkNetworkConnection() {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static int getColor() {
        return color;
    }

    public static boolean getWriter() {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        return preferences.getBoolean(PROPERTY_WRITER, false);
    }

    public static boolean is24Hour() {
        return is24Hours;
    }

    public static void setActivity(Context context) {
        AppManager.context = context;
        is24Hours = DateFormat.is24HourFormat(context);
    }

    public static String getSecret() {
        return SECRET;
    }

    public static void setWriter(boolean writer) {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(PROPERTY_WRITER, writer);
        editor.apply();
    }

    public static void updateColor() {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        color = setNextColor(preferences);
    }

    private static int setNextColor(SharedPreferences preferences) {
        int[] colors = context.getResources().getIntArray(R.array.colors_500);
        int colorPosition = preferences.getInt(PROPERTY_COLOR, 0);
        if (colorPosition == colors.length - 1)
            colorPosition = 0;
        else
            colorPosition++;
        Editor editor = preferences.edit();
        editor.putInt(PROPERTY_COLOR, colorPosition);
        editor.apply();

        return colors[colorPosition];
    }
}
