package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallsControlHelper {

    private static final String TAG = "MADARA";
    private final Context context;
    private CallScreeningService callScreeningService;
    private Call.Details callDetails;
    private final String phoneNumber;
    private final String countryNameCode;

    public interface OnDataReceivedListener {
        void onReceived(JSONObject callerInfo);
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted(boolean isSuccessful);
    }

    public CallsControlHelper(CallScreeningService callScreeningService, Call.Details callDetails, String phoneNumber) {
        this.context = callScreeningService.getApplicationContext();

        this.callScreeningService = callScreeningService;
        this.callDetails = callDetails;
        this.phoneNumber = phoneNumber;

        LoginSaverPrefHelper loginSaverPrefHelper = new LoginSaverPrefHelper(context);
        countryNameCode = loginSaverPrefHelper.getCountryNameCode();
    }

    public CallsControlHelper(Context context, String phoneNumber, String countryNameCode) {
        this.context = context;
        this.phoneNumber = phoneNumber;
        this.countryNameCode = countryNameCode;
    }

//    ----------------------------------------------------------------------------------------------

    public void blockAllSpamCalls(CallScreeningService.CallResponse.Builder response, OnTaskCompletedListener listener) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (callDetails.getCallerNumberVerificationStatus() == Connection.VERIFICATION_STATUS_FAILED) {
                response.setDisallowCall(true);
                response.setRejectCall(true);
                callScreeningService.respondToCall(callDetails, response.build());
                listener.onTaskCompleted(true);
            }
        }

        if (ContactUtils.getContactNameByPhoneNumber(context, phoneNumber).isEmpty()) {
            getCallerInfo(callerInfo -> {
                if (callerInfo != null) {
                    if (callerInfo.has("isSpamCall")) {
                        response.setDisallowCall(true);
                        response.setRejectCall(true);
                        callScreeningService.respondToCall(callDetails, response.build());
                        listener.onTaskCompleted(true);
                    }
                }
            });
        }
    }

//    ----------------------------------------------------------------------------------------------

    public void blockTopSpamCalls(CallScreeningService.CallResponse.Builder response, OnTaskCompletedListener listener) {
        if (ContactUtils.getContactNameByPhoneNumber(context, phoneNumber).isEmpty()) {
            getCallerInfo(callerInfo -> {
                if (callerInfo != null) {
                    if (callerInfo.has("spamType")) {
                        try {
                            String spamType = callerInfo.getString("spamType");
                            if (spamType.toLowerCase().contains("top")) {
                                response.setDisallowCall(true);
                                response.setRejectCall(true);
                                callScreeningService.respondToCall(callDetails, response.build());
                                listener.onTaskCompleted(true);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "blockTopSpamCalls: ", e);
                        }
                    }
                }
            });
        }
    }

//    ----------------------------------------------------------------------------------------------

    public void rejectAllIncomingCalls(CallScreeningService.CallResponse.Builder response, OnTaskCompletedListener listener) {
        response.setRejectCall(true);
        response.setDisallowCall(true);
        callScreeningService.respondToCall(callDetails, response.build());
        listener.onTaskCompleted(true);
    }

    public void rejectUnknownIncomingCalls(CallScreeningService.CallResponse.Builder response, OnTaskCompletedListener listener) {
        if (ContactUtils.getContactNameByPhoneNumber(context, phoneNumber).isEmpty()) {
            response.setRejectCall(true);
            response.setDisallowCall(true);
            callScreeningService.respondToCall(callDetails, response.build());
            listener.onTaskCompleted(true);
        }
    }

//    ----------------------------------------------------------------------------------------------

    public void getCallerInfo(OnDataReceivedListener listener) {

        GetPhoneNumberInfo getPhoneNumberInfo = new GetPhoneNumberInfo(context, phoneNumber, countryNameCode);

        getPhoneNumberInfo.getNumberInfo((isSuccessful, message, numberInfo) -> {

            if (isSuccessful) {
                try {
                    JSONObject callerInfo = new JSONObject();

                    JSONArray data = numberInfo.getJSONArray("data");
                    JSONObject firstData = data.getJSONObject(0);

                    if (firstData.has("name")) {
                        String callerName = firstData.getString("name");
                        callerInfo.put("callerName", callerName);
                    } else {
                        callerInfo.put("callerName", phoneNumber);
                    }

                    if (firstData.has("image")) {
                        String callerProfileImageLink = firstData.getString("image");
                        callerInfo.put("callerProfileImageLink", callerProfileImageLink);
                    }

                    if (firstData.has("addresses")) {
                        JSONArray addresses = firstData.getJSONArray("addresses");
                        if (addresses.length() > 0) {
                            if (addresses.getJSONObject(0).has("city")) {
                                String address = addresses.getJSONObject(0).getString("city");
                                if (addresses.getJSONObject(0).has("countryCode")) {
                                    String countryCode = addresses.getJSONObject(0).getString("countryCode");
                                    String countryName = CustomMethods.getCountryNameByCountryNameCode(countryCode);
                                    address += ", " + countryName;
                                }
                                callerInfo.put("address", address);
                            }
                        }
                    }

                    if (firstData.has("spamInfo")) {

                        callerInfo.put("isSpamCall", true);

                        JSONObject spamInfo = firstData.getJSONObject("spamInfo");
                        if (spamInfo.has("spamScore")) {
                            int spamScore = spamInfo.getInt("spamScore");
                            callerInfo.put("spamScore", spamScore);
                        }
                        if (spamInfo.has("spamType")) {
                            String spamType = spamInfo.getString("spamType");
                            callerInfo.put("spamType", spamType);
                        }
                    }

                    listener.onReceived(callerInfo);

                } catch (JSONException e) {
                    Log.e(TAG, "blockAllSpamCalls: ", e);
                    listener.onReceived(null);
                }
            } else {
                listener.onReceived(null);
            }
        });
    }
}
