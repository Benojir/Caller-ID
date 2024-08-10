package zorro.dimyon.calleridentity.activities;

import static zorro.dimyon.calleridentity.helpers.CustomMethods.isValidPhoneNumber;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import zorro.dimyon.calleridentity.R;
import zorro.dimyon.calleridentity.databinding.ActivityLoginBinding;
import zorro.dimyon.calleridentity.helpers.CustomMethods;
import zorro.dimyon.calleridentity.helpers.LoginHelper;
import zorro.dimyon.calleridentity.helpers.LoginSaverPrefHelper;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginSaverPrefHelper loginSaverPrefHelper;
    private final String TAG = "MADARA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        ------------------------------------------------------------------------------------------
        MaterialToolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.login));
        }
        toolbar.setTitleCentered(true);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//        ------------------------------------------------------------------------------------------

        loginSaverPrefHelper = new LoginSaverPrefHelper(this);

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

                LoginHelper loginHelper = new LoginHelper(this);

                loginHelper.requestOtp(justNumber, dialingCode, countryNameCode, new LoginHelper.OnOTPSentListener() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        pd.dismiss();
                        binding.getOtpContainer.setVisibility(View.GONE);
                        binding.verifyOtpContainer.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();

                        String requestId = data.optString("requestId");
                        loginSaverPrefHelper.saveOTPRequestId(requestId);
                        loginSaverPrefHelper.saveNumber(justNumber);
                        loginSaverPrefHelper.saveDialingCode(dialingCode);
                        loginSaverPrefHelper.saveCountryNameCode(countryNameCode);
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

                JSONObject data = new JSONObject();
                try {
                    data.put("token", otp);
                    data.put("requestId", loginSaverPrefHelper.getOTPRequestId());
                    data.put("phoneNumber", loginSaverPrefHelper.getNumber());
                    data.put("dialingCode", loginSaverPrefHelper.getDialingCode());
                    data.put("countryCode", loginSaverPrefHelper.getCountryNameCode());

                    LoginHelper loginHelper = new LoginHelper(this);

                    loginHelper.verifyOtp(data, (isVerified, message) -> {
                        pd.dismiss();

                        if (isVerified) {
                            loginSaverPrefHelper.saveApiKey(message);

                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "OTP verified successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            CustomMethods.errorAlert(this, "Error", message, "OK", false);
                        }
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "onCreate: ", e);
                    pd.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    CustomMethods.errorAlert(this, "Error", e.getMessage(), "OK", false);
                }
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
}