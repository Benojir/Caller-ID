package zorro.dimyon.calleridentity.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import zorro.dimyon.calleridentity.services.PopupService;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                // Call ended or no call present
                Log.d(TAG, "Call disconnected");
                // Handle the call disconnection event here
                context.stopService(new Intent(context, PopupService.class));
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                // Call started
                Log.d(TAG, "Call started");
            } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                // Incoming call
                Log.d(TAG, "Incoming call");
            }
        }
    }
}
