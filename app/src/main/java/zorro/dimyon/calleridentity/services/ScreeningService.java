package zorro.dimyon.calleridentity.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import zorro.dimyon.calleridentity.helpers.CallsControlHelper;
import zorro.dimyon.calleridentity.helpers.ContactUtils;

public class ScreeningService extends CallScreeningService {
    private static final String TAG = "MADARA";

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isIncoming = callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING;
        boolean isOutgoing = callDetails.getCallDirection() == Call.Details.DIRECTION_OUTGOING;

        Uri handle = callDetails.getHandle();
        String phoneNumber = handle.getSchemeSpecificPart();

        CallsControlHelper controlHelper = new CallsControlHelper(this, callDetails, phoneNumber);

        if (isIncoming) {

            if (preferences.getBoolean("block_all_spammers", false)) {
                controlHelper.blockAllSpamCalls();
            } else {
                if (preferences.getBoolean("block_top_spammers", false)) {
                    controlHelper.blockTopSpamCalls();
                }
            }

            if (preferences.getBoolean("reject_all_incoming_calls", false)) {
                controlHelper.rejectAllIncomingCalls();
            }
            if (preferences.getBoolean("reject_unknown_incoming_calls", false)) {
                controlHelper.rejectUnknownIncomingCalls();
            }

            if (preferences.getBoolean("floating_window_incoming", false)) {
                if (ContactUtils.getContactNameByPhoneNumber(this, phoneNumber).isEmpty()) {
                    showFloatingCallerInfoWindow(controlHelper, phoneNumber);
                } else {
                    showFloatingWindowForSavedContacts(phoneNumber);
                }
            } else {
                showFloatingCallerInfoWindow(controlHelper, phoneNumber);
            }
        }

        if (isOutgoing) {
            if (preferences.getBoolean("floating_window_outgoing", false)) {
                if (ContactUtils.getContactNameByPhoneNumber(this, phoneNumber).isEmpty()) {
                    showFloatingCallerInfoWindow(controlHelper, phoneNumber);
                } else {
                    showFloatingWindowForSavedContacts(phoneNumber);
                }
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    private void showFloatingWindowForSavedContacts(String phoneNumber) {

        String callerName = ContactUtils.getContactNameByPhoneNumber(this, phoneNumber);
        String callerProfileImageLink = "";
        String address = "";
        boolean isSpamCall = false;
        String spamType = "";
        String spamScore = "";

        Intent intent = new Intent(this, PopupService.class);
        intent.putExtra("callerName", callerName);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("callerProfileImageLink", callerProfileImageLink);
        intent.putExtra("address", address);
        intent.putExtra("isSpamCall", isSpamCall);
        intent.putExtra("spamType", spamType);
        intent.putExtra("spamScore", spamScore);
        startForegroundService(intent);
    }
//    ----------------------------------------------------------------------------------------------

    private void showFloatingCallerInfoWindow(CallsControlHelper controlHelper, String phoneNumber) {

        controlHelper.getCallerInfo(callerInfo -> {

            if (callerInfo != null) {

                try {
                    String callerName = callerInfo.getString("callerName");
                    String callerProfileImageLink = "";
                    String address = "";
                    boolean isSpamCall = false;
                    String spamType = "";
                    String spamScore = "";

                    if (callerInfo.has("callerProfileImageLink")) {
                        callerProfileImageLink = callerInfo.getString("callerProfileImageLink");
                    }

                    if (callerInfo.has("address")) {
                        address = callerInfo.getString("address");
                    }

                    if (callerInfo.has("isSpamCall")) {
                        isSpamCall = callerInfo.getBoolean("isSpamCall");
                    }

                    if (callerInfo.has("spamType")) {
                        spamType = callerInfo.getString("spamType");
                    }

                    if (callerInfo.has("spamScore")) {
                        spamScore = callerInfo.getString("spamScore");
                    }

                    Intent intent = new Intent(this, PopupService.class);
                    intent.putExtra("callerName", callerName);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("callerProfileImageLink", callerProfileImageLink);
                    intent.putExtra("address", address);
                    intent.putExtra("isSpamCall", isSpamCall);
                    intent.putExtra("spamType", spamType);
                    intent.putExtra("spamScore", spamScore);
                    startForegroundService(intent);

                } catch (JSONException e) {
                    Log.e(TAG, "onScreenCall: ", e);
                }
            }
        });
    }
}
