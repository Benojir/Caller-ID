package zorro.dimyon.calleridentity.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import zorro.dimyon.calleridentity.BuildConfig;
import zorro.dimyon.calleridentity.R;
import zorro.dimyon.calleridentity.helpers.SwipeDismissLayout;

public class PopupService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = BuildConfig.APPLICATION_ID + ".PopupServiceChannel";
    private static final String TAG = "MADARA";

    private WindowManager windowManager;
    private View popupView;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String phoneNumber = intent.getStringExtra("phone_number");
        showPopup(phoneNumber);
        return START_NOT_STICKY;
    }

    private void showPopup(String phoneNumber) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        FrameLayout tempRoot = new FrameLayout(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.floating_caller_info, tempRoot, false);

        TextView callerNameTV = popupView.findViewById(R.id.callerNameTV);
        callerNameTV.setText(phoneNumber);

        SwipeDismissLayout swipeLayout = (SwipeDismissLayout) popupView;
        swipeLayout.setOnDismissListener(() -> {
            windowManager.removeView(popupView);
            stopSelf();
        });

        try {
            windowManager.addView(popupView, params);
        } catch (Exception e) {
            Log.e(TAG, "showPopup: ", e);
        }
    }

    private Notification createNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call Popup")
                .setContentText("Displaying call information")
                .setSmallIcon(R.drawable.notifications_active_24)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Popup Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (popupView != null && popupView.isAttachedToWindow()) {
            windowManager.removeView(popupView);
        }
    }
}