package badtzmarupekkle.littlethings.application;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.format.DateFormat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.writer.writerendpoint.Writerendpoint;
import com.littlethings.endpoint.writer.writerendpoint.model.Response;
import com.littlethings.endpoint.writer.writerendpoint.model.Writer;

import java.io.File;
import java.io.IOException;

import badtzmarupekkle.littlethings.R;

public class AppManager {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String GCM_SENDER_ID = "353726664694";
    private static final String PROPERTY_COLOR = "Color";
    private static final String PROPERTY_GCM_REGISTRATION_VERSION = "GCMRegistrationVersion";
    private static final String PROPERTY_GCM_REGISTRATION_ID = "GCMRegistrationId";
    private static final String PROPERTY_WRITER = "Writer";
    private static final String SECRET = "PektzMaru";
    private static final String USER = "USER";

    private static boolean is24Hours;

    private static int colorPosition;
    private static int colorsSize;
    private static int color;
    private static int secondaryColor;

    private static Context context;

    public static void checkGCMRegistration() {
        if (getGCMRegistrationId() != null)
            return;
        registerGCM();
    }

    public static boolean checkNetworkConnection() {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) context, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    public static File getAppImagesFileDirectory() {
        File dirFile = new File(Environment.getExternalStorageDirectory() + "/BadtzMaruPekkle/LittleThings/Images");
        if (!dirFile.isDirectory()) {
            dirFile.mkdirs();
        }
        return dirFile;
    }

    public static File getAppFileDirectory() {
        File dirFile = new File(Environment.getExternalStorageDirectory() + "/BadtzMaruPekkle/LittleThings");
        if (!dirFile.isDirectory()) {
            dirFile.mkdirs();
        }
        return dirFile;
    }

    public static int getAppVersion() {
        try {
            return context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    public static int getColor() {
        return color;
    }

    public static int getColor(int position) {
        int[] colors = context.getResources().getIntArray(R.array.colors_500);
        return colors[position];
    }

    public static int getColorPosition() {
        return colorPosition;
    }

    public static int getColorsSize() {
        return colorsSize;
    }

    public static int[] getComplementColors() {
        int[] complementColors = new int[4];

        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        int colorPosition = preferences.getInt(PROPERTY_COLOR, 0);

        TypedArray ids = context.getResources().obtainTypedArray(R.array.colors_100);
        complementColors[0] = ids.getResourceId(colorPosition, 0);
        ids.recycle();

        ids = context.getResources().obtainTypedArray(R.array.colors_300);
        complementColors[1] = ids.getResourceId(colorPosition, 0);
        ids.recycle();

        ids = context.getResources().obtainTypedArray(R.array.colors_700);
        complementColors[2] = ids.getResourceId(colorPosition, 0);
        ids.recycle();

        ids = context.getResources().obtainTypedArray(R.array.colors_900);
        complementColors[3] = ids.getResourceId(colorPosition, 0);
        ids.recycle();

        return complementColors;
    }

    public static String getGCMRegistrationId() {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        String regId = preferences.getString(PROPERTY_GCM_REGISTRATION_ID, null);

        int regVersion = preferences.getInt(PROPERTY_GCM_REGISTRATION_VERSION, -1);
        int currVersion = getAppVersion();

        if (regVersion != currVersion)
            return null;

        return regId;
    }

    public static String getGCMSenderId() {
        return GCM_SENDER_ID;
    }

    public static int getSecondaryColor() { return secondaryColor; }

    public static int getSecondaryColor(int position) {
        int[] colors = context.getResources().getIntArray(R.array.colors_300);
        return colors[position];
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

    public static void setGCMRegistrationId(String regId) {
        SharedPreferences preferences = context.getSharedPreferences(USER, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(PROPERTY_GCM_REGISTRATION_VERSION, getAppVersion());
        editor.putString(PROPERTY_GCM_REGISTRATION_ID, regId);
        editor.apply();
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
        int[] colors = setNextColor(preferences);
        colorPosition = colors[0];
        colorsSize = colors[1];
        color = colors[2];
        secondaryColor = colors[3];
    }

    private static void registerGCM() {
        String regId;
        try {
            regId = GoogleCloudMessaging.getInstance(context).register(AppManager.getGCMSenderId());
        } catch (IOException e) {
            regId = null;
        }

        Writerendpoint.Builder builder = new Writerendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        Writerendpoint endpoint = builder.build();

        Writer writer = new Writer();
        writer.setRegistrationId(regId);
        writer.setSecret(getSecret());
        writer.setWriter(getWriter());

        try {
            Response response = endpoint.register(writer).execute();
            if (!response.getSuccess()) {
                GoogleCloudMessaging.getInstance(context).unregister();
                regId = null;
            }
        } catch (IOException e) {
            try {
                GoogleCloudMessaging.getInstance(context).unregister();
            } catch (IOException e1) {
            }
            regId = null;
        }

        if (regId != null)
            AppManager.setGCMRegistrationId(regId);
    }

    private static int[] setNextColor(SharedPreferences preferences) {
        int[] colors = context.getResources().getIntArray(R.array.colors_500);
        int[] secondaryColors = context.getResources().getIntArray(R.array.colors_300);
        int colorPosition = preferences.getInt(PROPERTY_COLOR, 0);
        if (colorPosition == colors.length - 1)
            colorPosition = 0;
        else
            colorPosition++;
        Editor editor = preferences.edit();
        editor.putInt(PROPERTY_COLOR, colorPosition);
        editor.apply();

        return new int[] {colorPosition, colors.length, colors[colorPosition], secondaryColors[colorPosition]};
    }
}
