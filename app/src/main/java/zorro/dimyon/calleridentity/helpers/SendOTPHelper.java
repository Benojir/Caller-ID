package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SendOTPHelper {
    private final Context context;
    private final String phoneNumber;
    private final String countryCode;
    private final int dialingCode;

    public interface OnDataRetrievedListener {
        void onSuccess(String response);

        void onFailure(String errorMessage);
    }

    public SendOTPHelper(Context context, String phoneNumber, String countryCode, int dialingCode) {
        this.context = context;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.dialingCode = dialingCode;
    }

    public void sendOTP(OnDataRetrievedListener listener) {

        try {
            JSONObject data = new JSONObject();
            data.put("countryCode", countryCode);
            data.put("dialingCode", dialingCode);

            JSONObject installationDetails = new JSONObject();
            JSONObject app = new JSONObject();
            app.put("buildVersion", 5);
            app.put("majorVersion", 11);
            app.put("minorVersion", 7);
            app.put("store", "GOOGLE_PLAY");
            installationDetails.put("app", app);

            JSONObject device = new JSONObject();
            device.put("deviceId", CustomMethods.getDeviceId(context));
            device.put("language", "en");
            device.put("manufacturer", Build.MANUFACTURER);
            device.put("model", Build.MODEL);
            device.put("osName", "Android");
            device.put("osVersion", "10");
            device.put("mobileServices", new JSONArray().put("GMS"));
            installationDetails.put("device", device);
            installationDetails.put("language", "en");

            data.put("installationDetails", installationDetails);
            data.put("phoneNumber", phoneNumber);
            data.put("region", "region-2");
            data.put("sequenceNo", 2);

            Log.d("MADARA", "sendOTP: " + data);

            String POST_URL = "https://account-asia-south1.truecaller.com/v2/sendOnboardingOtp";
            MediaType JSON = MediaType.get("application/json; charset=UTF-8");

            RequestBody body = RequestBody.create(data.toString(), JSON);

            Request request = new Request.Builder()
                    .url(POST_URL)
                    .post(body)
                    .addHeader("content-type", "application/json; charset=UTF-8")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("user-agent", "Truecaller/11.75.5 (Android;10)")
                    .addHeader("clientsecret", "lvc22mp3l1sfv6ujg83rd17btt")
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        try {
                            ResponseBody responseBody = response.body();
                            String errorBody = responseBody != null ? responseBody.string() : "No error body";
                            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("HTTP error: " + response.code() + " - " + errorBody));
                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An error occurred: " + e.getMessage()));
                        }
                        return;
                    }

                    try {
                        ResponseBody responseBody = response.body();

                        if (responseBody == null) {
                            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("No response body"));
                            return;
                        }

                        byte[] responseBodyBytes = responseBody.bytes();

                        String responseString;

                        if (CustomMethods.isGzipEncoded(responseBodyBytes)) {
                            responseString = CustomMethods.decompressGzip(responseBodyBytes);
                        } else {
                            responseString = new String(responseBodyBytes);
                        }

                        new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(responseString));
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An error occurred: " + e.getMessage()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An error occurred: " + e.getMessage()));
                }
            });

        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An error occurred: " + e.getMessage()));
        }
    }
}
