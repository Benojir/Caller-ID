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
import zorro.dimyon.calleridentity.helpers.SendOTPHelper;

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

            binding.getOtpButton.setOnClickListener(v -> {

                int dialingCode = Integer.parseInt(binding.ccp.getSelectedCountryCode());
                String countryNameCode = binding.ccp.getSelectedCountryNameCode();
                String fullPhoneNumber = binding.ccp.getFullNumberWithPlus();
                String justNumber = binding.phoneEditText.getText().toString().replaceAll("\\s", "");

                if (isValidPhoneNumber(fullPhoneNumber)) {
                    requestOtp(justNumber, dialingCode, countryNameCode);
                } else {
                    Toast.makeText(activity, "Invalid phone number", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return binding.getRoot();
    }

//    ----------------------------------------------------------------------------------------------

    private void requestOtp(String justNumber, int dialingCode, String countryNameCode) {

        binding.getOtpContainer.setVisibility(View.GONE);
        binding.verifyOtpContainer.setVisibility(View.VISIBLE);


        SendOTPHelper sendOTPHelper = new SendOTPHelper(activity, justNumber, countryNameCode, dialingCode);
        sendOTPHelper.sendOTP(new SendOTPHelper.OnDataRetrievedListener() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "onSuccess: " + response);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d(TAG, "onFailure: " + errorMessage);
            }
        });
    }
}