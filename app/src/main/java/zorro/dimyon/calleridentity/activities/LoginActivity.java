package zorro.dimyon.calleridentity.activities;

import static zorro.dimyon.calleridentity.helpers.CustomMethods.isValidPhoneNumber;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import zorro.dimyon.calleridentity.databinding.ActivityLoginBinding;
import zorro.dimyon.calleridentity.helpers.CustomMethods;
import zorro.dimyon.calleridentity.helpers.LoginSaver;
import zorro.dimyon.calleridentity.helpers.SendOTPHelper;
import zorro.dimyon.calleridentity.helpers.VerifyOTPHelper;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginSaver loginSaver;
    private final String TAG = "MADARA";
    private final JSONObject data = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginSaver = new LoginSaver(this);

        binding.ccp.registerCarrierNumberEditText(binding.phoneEditText);

        binding.getOtpButton.setOnClickListener(v -> {
            int dialingCode = Integer.parseInt(binding.ccp.getSelectedCountryCode());
            String countryNameCode = binding.ccp.getSelectedCountryNameCode();
            String fullPhoneNumber = binding.ccp.getFullNumberWithPlus();
            String justNumber = binding.phoneEditText.getText().toString().replaceAll("\\s", "");

            if (isValidPhoneNumber(fullPhoneNumber)) {

                ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Sending OTP...");
                pd.setCancelable(false);
                pd.show();

                requestOtp(justNumber, dialingCode, countryNameCode, new OnOTPSentListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        pd.dismiss();
                        binding.getOtpContainer.setVisibility(View.GONE);
                        binding.verifyOtpContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        CustomMethods.errorAlert(LoginActivity.this, "Error", errorMessage, "OK", false);
                    }
                });
            } else {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            }
        });

        binding.verifyOtpButton.setOnClickListener(v -> {
            String otp = binding.otpEditText.getText().toString().trim();

            if (CustomMethods.isValidOTP(otp)) {

                ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Verifying OTP...");
                pd.setCancelable(false);
                pd.show();

                verifyOtp(data, (isVerified, message) -> {
                    pd.dismiss();

                    if (isVerified) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        CustomMethods.errorAlert(this, "Error", message, "OK", false);
                    }
                });
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        binding.alreadyHasOTP.setOnClickListener(v -> {
            binding.getOtpContainer.setVisibility(View.GONE);
            binding.verifyOtpContainer.setVisibility(View.VISIBLE);
        });
    }

    //    ----------------------------------------------------------------------------------------------

    private void requestOtp(String justNumber, int dialingCode, String countryNameCode, OnOTPSentListener listener) {

        SendOTPHelper sendOTPHelper = new SendOTPHelper(this, justNumber, countryNameCode, dialingCode);

        sendOTPHelper.sendOTP(new SendOTPHelper.OnDataRetrievedListener() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject responseObject = new JSONObject(response);

                    if (responseObject.has("status")) {
                        int status = responseObject.getInt("status");

                        if (status == 1) {
                            binding.getOtpContainer.setVisibility(View.GONE);
                            binding.verifyOtpContainer.setVisibility(View.VISIBLE);

                            String requestId = responseObject.getString("requestId");

                            data.put("countryCode", countryNameCode);
                            data.put("dialingCode", dialingCode);
                            data.put("phoneNumber", justNumber);
                            data.put("requestId", requestId);

                            loginSaver.saveCountryNameCode(countryNameCode);

                            listener.onSuccess(data);
                        } else {
                            listener.onFailure("Failed to send OTP");
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

    private void verifyOtp(JSONObject data, OnOTPVerifiedListener listener) {

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

                                        loginSaver.saveLogin(installationId);
                                        listener.onComplete(true, "OTP verified");
                                    } else {
                                        listener.onComplete(false, "Installation ID not found");
                                    }
                                }
                            }
                        } else {
                            listener.onComplete(false, "Failed to verify OTP");
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

    public interface OnOTPSentListener {
        void onSuccess(JSONObject data);
        void onFailure(String errorMessage);
    }

    public interface OnOTPVerifiedListener {
        void onComplete(boolean isVerified, String message);
    }
}