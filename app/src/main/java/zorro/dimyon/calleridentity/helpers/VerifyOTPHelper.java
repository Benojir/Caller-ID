package zorro.dimyon.calleridentity.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

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

public class VerifyOTPHelper {
    private static final String TAG = "MADARA";
    private final JSONObject data;

    public interface OnDataRetrievedListener {
        void onSuccess(String response);

        void onFailure(String errorMessage);
    }

    public VerifyOTPHelper(JSONObject data) {
        this.data = data;
    }

    public void verifyOTP(OnDataRetrievedListener listener) {

        try {
            String POST_URL = "https://account-asia-south1.truecaller.com/v1/verifyOnboardingOtp";
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
                            byte[] responseBodyBytes = responseBody != null ? responseBody.bytes() : null;

                            String errorBody;

                            if (responseBodyBytes != null && CustomMethods.isGzipEncoded(responseBodyBytes)) {
                                errorBody = CustomMethods.decompressGzip(responseBodyBytes);
                            } else {
                                errorBody = new String(responseBodyBytes);
                            }
                            String finalErrorBody = errorBody;

                            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("HTTP error: " + response.code() + " - " + finalErrorBody));
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
                    new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An HTTP error occurred: " + e.getMessage()));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "verifyOTP: ", e);
            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An error occurred: " + e.getMessage()));
        }
    }

//    ----------------------------------------------------------------------------------------------

    public void completeOnboarding(JSONObject data, OnDataRetrievedListener listener) {
        try {
            String POST_URL = "https://account-noneu.truecaller.com/v1/completeOnboarding";
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
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "onFailure: ", e);
                    listener.onFailure("An error occurred: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    if (!response.isSuccessful()) {
                        try {
                            ResponseBody responseBody = response.body();
                            byte[] responseBodyBytes = responseBody != null ? responseBody.bytes() : null;

                            String errorBody;

                            if (responseBodyBytes != null && CustomMethods.isGzipEncoded(responseBodyBytes)) {
                                errorBody = CustomMethods.decompressGzip(responseBodyBytes);
                            } else {
                                errorBody = new String(responseBodyBytes);
                            }
                            String finalErrorBody = errorBody;

                            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("HTTP error: " + response.code() + " - " + finalErrorBody));
                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("An error occurred: " + e.getMessage()));
                        }
                    } else {
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
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "completeOnboarding: ", e);
            listener.onFailure("An error occurred: " + e.getMessage());
        }
    }
}
