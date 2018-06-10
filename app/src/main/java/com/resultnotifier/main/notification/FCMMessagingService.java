package com.resultnotifier.main.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.resultnotifier.main.R;

public class FCMMessagingService extends FirebaseMessagingService {
    private static final String TAG = "REN_FCMMessagingService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "FCMMessagingService started");
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        final RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification == null) {
            Log.i(TAG, "Received message. from=" + remoteMessage.getFrom());
            return;
        }

        final String title = notification.getTitle();
        final String body = notification.getBody();
        Log.i(TAG, "Received message. from=" + remoteMessage.getFrom() + ", title=" + title
                + ", body=" + body);

        sendNotification(title, body);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param body FCM message body received.
     */
    private void sendNotification(final String title, final String body) {

        final String channelId = "default";
        final Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_io_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e(TAG, "Unable to send notification because manager is null. title=" + title
            + ", body=" + body);
            return;
        }

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(channelId,
                    "Result Notifier Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Log.i(TAG, "Generating notification with title=" + title + ", body=" + body);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
