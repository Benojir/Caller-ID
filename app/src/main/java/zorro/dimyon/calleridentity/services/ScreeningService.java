package zorro.dimyon.calleridentity.services;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;

import androidx.annotation.NonNull;

public class ScreeningService extends CallScreeningService {
    private static final String TAG = "MADARA";

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {

        boolean isIncoming = callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING;

        Uri handle = callDetails.getHandle();

        String number = handle.getSchemeSpecificPart();

        if (isIncoming) {

            CallResponse.Builder response = new CallResponse.Builder();
            respondToCall(callDetails, response.build());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                switch (callDetails.getCallerNumberVerificationStatus()) {
                    case Connection.VERIFICATION_STATUS_FAILED:
                        // Network verification failed, likely an invalid/spam call.
                        response.setDisallowCall(true);
                        response.setRejectCall(true);
                        break;
                    case Connection.VERIFICATION_STATUS_PASSED:
                        // Network verification passed, likely a valid call.
                        break;
                    default:
                        // Network could not perform verification.
                        // This branch matches Connection.VERIFICATION_STATUS_NOT_VERIFIED
                }
            }

            Intent intent = new Intent(this, PopupService.class);
            intent.putExtra("phone_number", number);
            startForegroundService(intent);
            Log.d(TAG, "onScreenCall: " + number);
        }
    }


}
