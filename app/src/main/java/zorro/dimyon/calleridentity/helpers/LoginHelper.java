package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginHelper {
    private final String TAG = "MADARA";
    private final Context context;

    public interface OnOTPSentListener {
        void onSuccess(JSONObject data);

        void onFailure(String errorMessage);
    }

    public interface OnOTPVerifiedListener {
        void onComplete(boolean isVerified, String message);
    }

    public LoginHelper(Context context) {
        this.context = context;
    }

    public void requestOtp(String justNumber, int dialingCode, String countryNameCode, OnOTPSentListener listener) {

        SendOTPHelper sendOTPHelper = new SendOTPHelper(context, justNumber, countryNameCode, dialingCode);

        sendOTPHelper.sendOTP(new SendOTPHelper.OnDataRetrievedListener() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);

                    if (responseObject.has("status")) {
                        int status = responseObject.getInt("status");

                        if (status == 1 || status == 9) {

                            String requestId = responseObject.getString("requestId");

                            JSONObject data = new JSONObject();
                            data.put("countryCode", countryNameCode);
                            data.put("dialingCode", dialingCode);
                            data.put("phoneNumber", justNumber);
                            data.put("requestId", requestId);

                            listener.onSuccess(data);
                        } else {

                            if (status == 5 || status == 6) {
                                listener.onFailure("Too many request attempted. Try again after 1 hour later.");
                                return;
                            }

                            listener.onFailure(responseObject.toString());
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Send OTP onSuccess: ", e);
                    listener.onFailure(e.getMessage());
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d(TAG, "Send OTP onFailure: " + errorMessage);
                listener.onFailure("Try again 1 hour later.");
            }
        });
    }

//    ----------------------------------------------------------------------------------------------

    public void verifyOtp(JSONObject data, OnOTPVerifiedListener listener) {

        VerifyOTPHelper verifyOTPHelper = new VerifyOTPHelper(data);

        verifyOTPHelper.verifyOTP(new VerifyOTPHelper.OnDataRetrievedListener() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);

                    if (responseObject.has("status")) {
                        int status = responseObject.getInt("status");

                        if (status == 2) {

                            if (responseObject.has("suspended")) {
                                boolean suspended = responseObject.getBoolean("suspended");

                                if (suspended) {
                                    listener.onComplete(false, "Your account has been suspended!");
                                } else {
                                    if (responseObject.has("installationId")) {
                                        String installationId = responseObject.getString("installationId");
                                        listener.onComplete(true, installationId);
                                    } else {
                                        listener.onComplete(false, "Installation ID not found.\n\n" + responseObject);
                                    }
                                }
                            }
                        } else if (status == 11 || status == 40101) {
                            listener.onComplete(false, "Invalid OTP");
                        } else if (status == 7) {
                            listener.onComplete(false, "Retries limit exceeded");

                        } else if (status == 17) {

                            verifyOTPHelper.completeOnboarding(data, new VerifyOTPHelper.OnDataRetrievedListener() {
                                @Override
                                public void onSuccess(String response) {

                                    try {

                                        JSONObject responseObject = new JSONObject(response);

                                        if (responseObject.has("installationId")) {
                                            String installationId = responseObject.getString("installationId");
                                            listener.onComplete(true, installationId);
                                        } else {
                                            listener.onComplete(false, "Installation Id not found. Failed completing signup.\n\n" + responseObject);
                                        }

                                    } catch (JSONException e) {
                                        Log.e(TAG, "onSuccess: ", e);
                                        listener.onComplete(false, e.getMessage());
                                    }
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.d(TAG, "Verify OTP onFailure: " + errorMessage);
                                    listener.onComplete(false, errorMessage);
                                }
                            });
                            
                        } else {
                            listener.onComplete(false, "Failed to verify OTP \n\n" + responseObject);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Verify OTP onSuccess: ", e);
                    listener.onComplete(false, e.getMessage());
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d(TAG, "Verify OTP onFailure: " + errorMessage);
                listener.onComplete(false, errorMessage);
            }
        });
    }

//    ----------------------------------------------------------------------------------------------
}
