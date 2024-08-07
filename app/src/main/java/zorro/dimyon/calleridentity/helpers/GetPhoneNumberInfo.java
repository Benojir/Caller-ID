package zorro.dimyon.calleridentity.helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GetPhoneNumberInfo {

    private static final String TAG = "MADARA";
    private final String phoneNumber;

    public interface OnFetchedInfoListener {
        void onSuccess(JSONObject numberInfo);
        void onError(String errorMessage);
    }

    public GetPhoneNumberInfo(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void getNumberInfo(OnFetchedInfoListener listener) {
        OkHttpClient client = new OkHttpClient();

        String API_KEY = "a2i0P--oPOCMh-2-IeBui8rjPb6i45xnOoPlVsDPycb6VQl2QtjR_6r0Q7Mn6HB1";
        Request request = new Request.Builder()
                .url("https://search5-noneu.truecaller.com/v2/search?q=" + phoneNumber + "&countryCode=IN&type=4&locAddr=&encoding=json")
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Bearer " + API_KEY)
                .addHeader("accept-encoding", "gzip")
                .addHeader("user-agent", "Truecaller/14.16.6 (Android;14)")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                Log.e(TAG, "onFailure: ", e);
                // Handle failure
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                    listener.onError("Response not successful");
                }

                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    // Check if the response is gzip encoded
                    byte[] responseBodyBytes = responseBody.bytes();
                    String responseString;
                    if (isGzipEncoded(responseBodyBytes)) {
                        responseString = decompressGzip(responseBodyBytes);
                    } else {
                        responseString = new String(responseBodyBytes);
                    }

                    // Output the response
                    System.out.println(responseString);
                }
            }
        });
    }

    private static boolean isGzipEncoded(byte[] bytes) {
        return bytes.length > 1 && bytes[0] == (byte) 0x1f && bytes[1] == (byte) 0x8b;
    }

    private static String decompressGzip(byte[] compressed) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        StringBuilder out = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) != -1) {
            out.append(new String(buffer, 0, len));
        }
        return out.toString();
    }

}
