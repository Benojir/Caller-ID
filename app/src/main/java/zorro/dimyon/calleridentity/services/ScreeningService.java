package zorro.dimyon.calleridentity.services;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import zorro.dimyon.calleridentity.helpers.ContactsHelper;
import zorro.dimyon.calleridentity.helpers.GetPhoneNumberInfo;
import zorro.dimyon.calleridentity.helpers.LoginSaver;

public class ScreeningService extends CallScreeningService {
    private static final String TAG = "MADARA";

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {

        boolean isIncoming = callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING;

        Uri handle = callDetails.getHandle();

        String number = handle.getSchemeSpecificPart();

        if (isIncoming) {

            CallResponse.Builder response = new CallResponse.Builder();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                switch (callDetails.getCallerNumberVerificationStatus()) {
                    case Connection.VERIFICATION_STATUS_FAILED:
                        // Network verification failed, likely an invalid/spam call.
                        response.setDisallowCall(true);
                        response.setRejectCall(true);
                        respondToCall(callDetails, response.build());
                        break;
                    case Connection.VERIFICATION_STATUS_PASSED:
                        // Network verification passed, likely a valid call.
                        break;
                    default:
                        // Network could not perform verification.
                        // This branch matches Connection.VERIFICATION_STATUS_NOT_VERIFIED
                }
            }

            if (ContactsHelper.getContactNameByPhoneNumber(this, number).isEmpty()){

                LoginSaver loginSaver = new LoginSaver(this);
                String countryNameCode = loginSaver.getCountryNameCode();
                String apiKey = loginSaver.getApiKey();

                GetPhoneNumberInfo callerInfo = new GetPhoneNumberInfo(this, number, countryNameCode, apiKey);
                callerInfo.getNumberInfo(new GetPhoneNumberInfo.OnFetchedInfoListener() {
                    @Override
                    public void onSuccess(JSONObject numberInfo) {

                        try {
                            String callerName = numberInfo.getString("callerName");
                            String address = numberInfo.getString("address");
                            boolean isSpamCall = numberInfo.getBoolean("isSpamCall");
                            String spamType = numberInfo.getString("spamType");

                            if (isSpamCall) {
                                response.setDisallowCall(true);
                                response.setRejectCall(true);
                                respondToCall(callDetails, response.build());

                                Toast.makeText(ScreeningService.this, "Spam call blocked.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Start the PopupService
                                Intent intent = new Intent(ScreeningService.this, PopupService.class);
                                intent.putExtra("caller_name", callerName);
                                intent.putExtra("phone_number", number);
                                intent.putExtra("address", address);
                                intent.putExtra("spam_type", spamType);
                                startForegroundService(intent);
                                Log.d(TAG, "onScreenCall: " + number);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "onSuccess: ", e);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d(TAG, "onError: " + errorMessage);
                    }
                });
            }
        }
    }
}
