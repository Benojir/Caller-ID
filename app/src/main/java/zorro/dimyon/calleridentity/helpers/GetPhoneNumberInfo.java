package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
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

    public interface OnFetchedInfoListener {
        void onSuccess(JSONObject numberInfo);
        void onError(String errorMessage);
    }

    public GetPhoneNumberInfo(Context context, String phoneNumber) {
        this.context = context;
        this.phoneNumber = phoneNumber;
    }

    public void getNumberInfo(OnFetchedInfoListener listener) {
        OkHttpClient client = new OkHttpClient();

        String API_KEY = context.getString(R.string.api_key);
        Request request = new Request.Builder()
                .url("https://search5-noneu.truecaller.com/v2/search?q=" + phoneNumber + "&countryCode=IN&type=4&locAddr=&encoding=json")
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Bearer " + API_KEY)
                .addHeader("accept-encoding", "gzip")
                .addHeader("user-agent", context.getString(R.string.truecaller_user_agent))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: ", e);
                // Handle failure
                new Handler(Looper.getMainLooper()).post(() -> listener.onError("Request failed"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                    new Handler(Looper.getMainLooper()).post(() -> listener.onError("Response not successful"));
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
                    Log.d(TAG, "onResponse: " + responseString);

                    try {
                        JSONObject numberData = new JSONObject(responseString);
                        JSONObject data = numberData.getJSONArray("data").getJSONObject(0);

                        String callerName = phoneNumber;
                        String address = "Unknown";
                        boolean isSpamCall = false;
                        String spamType = "";

                        if (data.has("name")) {
                            callerName = data.getString("name");
                        }

                        if (data.has("addresses")) {
                            JSONArray addresses = data.getJSONArray("addresses");
                            if (addresses.length() > 0) {
                                if (addresses.getJSONObject(0).has("city")){
                                    address = addresses.getJSONObject(0).getString("city");
                                }
                            }
                        }

                        if (data.has("spamInfo")) {
                            isSpamCall = true;
                            if (data.getJSONObject("spamInfo").has("spamType")){
                                spamType = data.getJSONObject("spamInfo").getString("spamType");
                            }
                        }

                        JSONObject numberInfo = new JSONObject();
                        numberInfo.put("callerName", callerName);
                        numberInfo.put("address", address);
                        numberInfo.put("isSpamCall", isSpamCall);
                        numberInfo.put("spamType", spamType);

                        new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(numberInfo));

                    } catch (JSONException e) {
                        Log.e(TAG, "onResponse: ", e);
                        listener.onError("JSON parsing error");
                    }
                }
            }
        });
    }

}
