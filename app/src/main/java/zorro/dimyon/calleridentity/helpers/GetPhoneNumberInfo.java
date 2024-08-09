package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zorro.dimyon.calleridentity.R;

public class GetPhoneNumberInfo {

    private final String TAG = "MADARA";
    private final Context context;
    private final String phoneNumber;
    private final String countryNameCode;
    private final String apiKey;

    public interface OnFetchedInfoListener {
        void onReceivedResponse(boolean isSuccessful, String message, JSONObject numberInfo);
    }

    public GetPhoneNumberInfo(Context context, String phoneNumber, String countryNameCode) {
        this.context = context;
        this.phoneNumber = phoneNumber;
        this.countryNameCode = countryNameCode;

        LoginSaverPrefHelper loginSaverPrefHelper = new LoginSaverPrefHelper(context);
        this.apiKey = loginSaverPrefHelper.getApiKey();
    }

    public void getNumberInfo(OnFetchedInfoListener listener) {

        if (apiKey.isEmpty() || !CustomMethods.isInternetAvailable(context)) {
            listener.onReceivedResponse(false, "API key is empty or internet is not available", null);
            return;
        }

        Request request = new Request.Builder()
                .url("https://search5-noneu.truecaller.com/v2/search?q=" + phoneNumber + "&countryCode=" + countryNameCode + "&type=4&locAddr=&encoding=json")
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Bearer " + apiKey)
                .addHeader("accept-encoding", "gzip")
                .addHeader("user-agent", context.getString(R.string.truecaller_user_agent))
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: ", e);
                // Handle failure
                new Handler(Looper.getMainLooper()).post(() -> listener.onReceivedResponse(false, e.getMessage(), null));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                    new Handler(Looper.getMainLooper()).post(() -> listener.onReceivedResponse(false, "Response code: " + response.code(), null));
                    return;
                }

                ResponseBody responseBody = response.body();

                if (responseBody != null) {
                    // Check if the response is gzip encoded
                    byte[] responseBodyBytes = responseBody.bytes();

                    String responseString;

                    if (CustomMethods.isGzipEncoded(responseBodyBytes)) {
                        responseString = CustomMethods.decompressGzip(responseBodyBytes);
                    } else {
                        responseString = new String(responseBodyBytes);
                    }

                    try {
                        JSONObject numberInfo = new JSONObject(responseString);
                        new Handler(Looper.getMainLooper()).post(() -> listener.onReceivedResponse(true, "Success", numberInfo));
                    } catch (JSONException e) {
                        listener.onReceivedResponse(false, responseString, null);
                        Log.e(TAG, "onResponse: ", e);
                    }
                }
            }
        });
    }
}
