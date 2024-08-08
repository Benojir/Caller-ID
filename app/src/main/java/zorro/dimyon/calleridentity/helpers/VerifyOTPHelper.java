package zorro.dimyon.calleridentity.helpers;

import android.content.Context;

import org.json.JSONObject;

public class VerifyOTPHelper {
    private final String API_ENDPOINT = "https://account-asia-south1.truecaller.com/v1/verifyOnboardingOtp";
    private final Context context;
    private final JSONObject requestBody;

    public interface OnDataRetrievedListener {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    public VerifyOTPHelper(Context context, JSONObject requestBody) {
        this.context = context;
        this.requestBody = requestBody;
    }

    public void verifyOTP(OnDataRetrievedListener listener) {

    }
}
