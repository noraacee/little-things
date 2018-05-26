package badtzmarupekkle.littlethings.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.activity.LogInActivity;
import badtzmarupekkle.littlethings.application.LittleThingsApplication;

public class GCMIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 1;

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE))
                createNotification(extras.toString());
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void createNotification(String msg) {
        if (!LittleThingsApplication.isVisible()) {
            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, LogInActivity.class), 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setContentTitle("Little Things")
                    .setContentText("New blog post!")
                    .setContentIntent(intent)
                    .setSmallIcon(R.drawable.notification);
            nManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
