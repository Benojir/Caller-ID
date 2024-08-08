package zorro.dimyon.calleridentity.fragments;

import static zorro.dimyon.calleridentity.helpers.CustomMethods.isValidPhoneNumber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zorro.dimyon.calleridentity.R;
import zorro.dimyon.calleridentity.databinding.FragmentLoginBinding;
import zorro.dimyon.calleridentity.helpers.CustomMethods;

public class LoginFragment extends Fragment {

    private static final String TAG = "MADARA";
    private FragmentLoginBinding binding;
    private Activity activity;
    private final String sendOtpPostApi = "https://account-asia-south1.truecaller.com/v2/sendOnboardingOtp";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        activity = getActivity();

        if (activity != null) {
            binding.ccp.registerCarrierNumberEditText(binding.phoneEditText);
            String dialingCode = binding.ccp.getSelectedCountryCode();

            binding.getOtpButton.setOnClickListener(v -> {
                String fullPhoneNumber = binding.ccp.getFullNumberWithPlus();
                if (isValidPhoneNumber(fullPhoneNumber)) {
                    // Request OTP
                    requestOtp(fullPhoneNumber, dialingCode);
                } else {
                    Toast.makeText(activity, "Invalid phone number", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return binding.getRoot();
    }

//    ----------------------------------------------------------------------------------------------

    private void requestOtp(String fullPhoneNumber, String dialingCode) {

        binding.getOtpContainer.setVisibility(View.GONE);
        binding.verifyOtpContainer.setVisibility(View.VISIBLE);

        String deviceId = CustomMethods.getDeviceId(activity);
        String countryCode = CustomMethods.getCountryCodeFromLocale(activity);
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;

//        new Thread(() -> {
            try {
                JSONObject app = new JSONObject();
                app.put("buildVersion", 8);
                app.put("majorVersion", 14);
                app.put("minorVersion", 16);
                app.put("store", "GOOGLE_PLAY");

                JSONArray mobileServices = new JSONArray();
                mobileServices.put("GMS");

                JSONObject device = new JSONObject();
                device.put("deviceId", deviceId);
                device.put("language", "en");
                device.put("manufacturer", manufacturer);
                device.put("model", model);
                device.put("osName", "Android");
                device.put("osVersion", osVersion);
                device.put("mobileServices", mobileServices);

                JSONObject installationDetails = new JSONObject();
                installationDetails.put("app", app);
                installationDetails.put("device", device);
                installationDetails.put("language", "en");

                JSONObject data = new JSONObject();
                data.put("countryCode", countryCode);
                data.put("dialingCode", dialingCode);
                data.put("phoneNumber", "9732422565");
                data.put("region", "region-2");
                data.put("sequenceNo", 2);
                data.put("installationDetails", installationDetails);


                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), data.toString());

                Request request = new Request.Builder()
                        .url(sendOtpPostApi)
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
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        Log.d(TAG, "onResponse: " + response.code());
                        if (response.isSuccessful()) {

                            ResponseBody responseBody = response.body();

                            if (responseBody != null) {
                                String responseString = responseBody.string();
                                Log.d(TAG, "responseString: " + responseString);
                            } else {
                                Log.e(TAG, "requestOtp: Response body is null");
                            }
                        } else {
                            Log.e(TAG, "requestOtp is not successful: " + response.message());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "requestOtp: ", e);
            }
//        }).start();
    }
}