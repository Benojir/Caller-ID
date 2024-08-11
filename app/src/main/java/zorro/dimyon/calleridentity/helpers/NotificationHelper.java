package zorro.dimyon.calleridentity.helpers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import zorro.dimyon.calleridentity.R;
import zorro.dimyon.calleridentity.activities.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "blocked_spam_call_notification_channel";
    private static final String CHANNEL_NAME = "Blocked Spam Call Notifications";
    private static final String CHANNEL_DESCRIPTION = "This channel is used for showing blocked spam calls notifications";

    private static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void showBlockedCallNotification(@NonNull Context context, JSONObject callerInfo, @NonNull String phoneNumber) {

        if (callerInfo == null) {
            return;
        }

        String callerName = phoneNumber;
        String address = phoneNumber;

        try {
            if (callerInfo.has("callerName")) {
                callerName = callerInfo.getString("callerName");
            }

            if (callerInfo.has("address")) {
                address = callerInfo.getString("address");
            }
        } catch (JSONException e) {
            Log.d("MADARA", "showBlockedCallNotification: " + e.getMessage());
        }

        String notificationTitle = "Blocked Spam Call (" + phoneNumber + ")";
        String notificationText = callerName + " (" + address + ")";

        createNotificationChannel(context); // Notification channel should be created before showing the notification.

        // Create an Intent to launch when the notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.block_24) // Replace with your own icon
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Automatically remove the notification when it's tapped

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // If permission is not granted, don't show the notification.
        }
        notificationManager.notify(1, builder.build());
    }
}
