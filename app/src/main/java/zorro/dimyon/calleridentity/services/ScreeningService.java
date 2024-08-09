package zorro.dimyon.calleridentity.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import zorro.dimyon.calleridentity.helpers.ContactUtils;
import zorro.dimyon.calleridentity.helpers.GetPhoneNumberInfo;
import zorro.dimyon.calleridentity.helpers.LoginSaverPrefHelper;

public class ScreeningService extends CallScreeningService {
    private static final String TAG = "MADARA";

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isIncoming = callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        boolean isOutgoing = callDetails.getCallDirection() == Call.Details.DIRECTION_OUTGOING;

        Uri handle = callDetails.getHandle();
        String number = handle.getSchemeSpecificPart();

        CallResponse.Builder response = new CallResponse.Builder();

        if (isIncoming) {

            if (preferences.getBoolean("block_all_spammers", false)) {

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (callDetails.getCallerNumberVerificationStatus() == Connection.VERIFICATION_STATUS_FAILED){
                    response.setDisallowCall(true);
                    response.setRejectCall(true);
                    respondToCall(callDetails, response.build());
                }
            }

            if (ContactUtils.getContactNameByPhoneNumber(this, number).isEmpty()) {


            }
        }

        if (isOutgoing) {

        }
    }
}
