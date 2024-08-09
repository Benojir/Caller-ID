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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String callerName = intent.getStringExtra("callerName");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String callerProfileImageLink = intent.getStringExtra("callerProfileImageLink");
        String address = intent.getStringExtra("address");
        boolean isSpamCall = intent.getBooleanExtra("isSpamCall", false);
        String spamType = intent.getStringExtra("spamType");

        if (callerProfileImageLink == null) {
            callerProfileImageLink = "";
        }

        if (spamType != null && isSpamCall && !spamType.isEmpty()) {
            address = address + " (" + spamType + ")";
        }

        startForeground(NOTIFICATION_ID, createNotification(callerName, phoneNumber));
        showPopup(callerName, address, isSpamCall, callerProfileImageLink);
        return START_NOT_STICKY;
    }

    private void showPopup(String callerName, String address, boolean isSpamCall, String profileImageLink) {
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

        SwipeDismissLayout floatingCallerInfoMainLayout = popupView.findViewById(R.id.floatingCallerInfoMainLayout);
        TextView callerNameTV = popupView.findViewById(R.id.callerNameTV);
        TextView callerAddressTV = popupView.findViewById(R.id.callerAddressTV);
        ImageView callerProfileIV = popupView.findViewById(R.id.imageView);

        callerNameTV.setText(callerName);
        callerAddressTV.setText(address);

        if (address.isEmpty()) {
            callerAddressTV.setVisibility(View.GONE);
        }

        if (!profileImageLink.isEmpty()) {
            Glide.with(this)
                    .load(profileImageLink)
                    .placeholder(R.drawable.verified_user_24)
                    .error(R.drawable.verified_user_24)
                    .into(callerProfileIV);
        } else {
            if (isSpamCall) {
                callerProfileIV.setImageResource(R.drawable.warning_24);
            }
        }

        if (isSpamCall) {
            floatingCallerInfoMainLayout.setBackgroundResource(R.drawable.background_danger_floating_caller_info);
        }

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

    private Notification createNotification(String callerName, String phoneNumber) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(callerName)
                .setContentText(phoneNumber)
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