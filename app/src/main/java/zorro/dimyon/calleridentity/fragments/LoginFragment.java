package zorro.dimyon.calleridentity.fragments;

import static zorro.dimyon.calleridentity.helpers.CustomMethods.isValidPhoneNumber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import org.json.JSONException;
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
    private JSONObject data;

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

            binding.verifyOtpButton.setOnClickListener(v -> {
                String otp = binding.otpEditText.getText().toString().trim();

                if (CustomMethods.isValidOTP(otp)) {
//                    verifyOtp(otp);
                } else {
                    Toast.makeText(activity, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return binding.getRoot();
    }

//    ----------------------------------------------------------------------------------------------

    private void requestOtp(String justNumber, int dialingCode, String countryNameCode) {

        ProgressDialog pd = new ProgressDialog(activity);
        pd.setMessage("Sending OTP...");
        pd.setCancelable(false);
        pd.show();

        SendOTPHelper sendOTPHelper = new SendOTPHelper(activity, justNumber, countryNameCode, dialingCode);

        sendOTPHelper.sendOTP(new SendOTPHelper.OnDataRetrievedListener() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);

                    if (responseObject.has("status")) {
                        int status = responseObject.getInt("status");

                        if (status == 1) {
                            Toast.makeText(activity, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            binding.getOtpContainer.setVisibility(View.GONE);
                            binding.verifyOtpContainer.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(activity, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onSuccess: ", e);
                    pd.dismiss();
                    Toast.makeText(activity, "JSON parsing error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d(TAG, "onFailure: " + errorMessage);
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

//    ----------------------------------------------------------------------------------------------

    private void verifyOtp(String otp, JSONObject data) {

    }
}