package zorro.dimyon.calleridentity.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import zorro.dimyon.calleridentity.databinding.ActivitySearchBinding;
import zorro.dimyon.calleridentity.helpers.CallsControlHelper;
import zorro.dimyon.calleridentity.helpers.CustomMethods;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "MADARA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(v -> onBackPressed());

        binding.inputPhoneNumberET.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.inputPhoneNumberET.getText().toString().isEmpty()) {

                binding.inputBoxContainer.setVisibility(View.GONE);
                binding.loaderProgressBar.setVisibility(View.VISIBLE);

                String phoneNumber = binding.inputPhoneNumberET.getText().toString().trim();

                if (CustomMethods.isValidPhoneNumber(phoneNumber)) {

                    int dialingCountryCode = CustomMethods.getCountryCode(this, phoneNumber);

                    if (dialingCountryCode == -1) {
                        Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    String countryISOCode = CustomMethods.getISOCodeByDialingCode(dialingCountryCode);

                    CallsControlHelper controlHelper = new CallsControlHelper(this, phoneNumber, countryISOCode);

                    controlHelper.getCallerInfo(callerInfo -> {

                        binding.inputBoxContainer.setVisibility(View.VISIBLE);
                        binding.loaderProgressBar.setVisibility(View.GONE);

                        try {
                            if (callerInfo != null) {

                                String callerName = phoneNumber;
                                String address = CustomMethods.getCountryNameByCountryNameCode(countryISOCode);
                                boolean isSpamCall = false;

                                if (callerInfo.has("callerName")) {
                                    callerName = callerInfo.getString("callerName");
                                }

                                if (callerInfo.has("address")) {
                                    address = callerInfo.getString("address");
                                }

                                binding.callerNameTV.setText(callerName);
                                binding.phoneNumberTV.setText(phoneNumber);
                                binding.callerLocationTV.setText(address);

                                if (callerInfo.has("isSpamCall")) {
                                    isSpamCall = callerInfo.getBoolean("isSpamCall");
                                }

                                if (isSpamCall) {
                                    binding.spamInfoTV.setVisibility(View.VISIBLE);

                                    String spamDescription = "☠️ Spam number";

                                    if (callerInfo.has("spamType")) {
                                        String spamType = callerInfo.getString("spamType");
                                        spamDescription += " (" + spamType + ")";
                                    }
                                    binding.spamInfoTV.setText(spamDescription);
                                }
                            } else {
                                binding.callerNameTV.setText(phoneNumber);
                                binding.phoneNumberTV.setText(phoneNumber);
                                binding.callerLocationTV.setText(CustomMethods.getCountryNameByCountryNameCode(countryISOCode));
                                binding.spamInfoTV.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onCreate: ", e);
                            Toast.makeText(this, "JSON Exception", Toast.LENGTH_SHORT).show();
                            binding.inputBoxContainer.setVisibility(View.GONE);
                        }
                    });

                } else {
                    Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            } else {
                return false;
            }
        });
    }
}