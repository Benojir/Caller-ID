package zorro.dimyon.calleridentity.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;

import zorro.dimyon.calleridentity.R;
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

//        ------------------------------------------------------------------------------------------
        MaterialToolbar toolbar = findViewById(R.id.toolbar_include);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.search_activity_title));
        }
        toolbar.setTitleCentered(true);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//        ------------------------------------------------------------------------------------------

        binding.clearBtn.setOnClickListener(v -> {
            binding.inputPhoneNumberET.setText("");
            CustomMethods.showKeyBoard(this, binding.inputPhoneNumberET);
        });

        binding.inputPhoneNumberET.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.inputPhoneNumberET.getText().toString().isEmpty()) {

                CustomMethods.hideKeyboard(this, binding.inputPhoneNumberET);
                binding.numberInfoCard.setVisibility(View.GONE);
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

                        binding.numberInfoCard.setVisibility(View.VISIBLE);
                        binding.loaderProgressBar.setVisibility(View.GONE);

                        try {
                            if (callerInfo != null) {

                                String callerName = phoneNumber;
                                String address = CustomMethods.getCountryNameByCountryNameCode(countryISOCode);
                                boolean isSpamCall = false;
                                String callerProfileImageLink = "";

                                if (callerInfo.has("callerName")) {
                                    callerName = callerInfo.getString("callerName");
                                }

                                if (callerInfo.has("address")) {
                                    address = callerInfo.getString("address");
                                }

                                binding.callerNameTV.setText(callerName);
                                binding.phoneNumberTV.setText(phoneNumber);
                                binding.callerLocationTV.setText(address);

                                if (callerInfo.has("callerProfileImageLink")) {
                                    callerProfileImageLink = callerInfo.getString("callerProfileImageLink");

                                    Glide.with(this)
                                            .load(callerProfileImageLink)
                                            .placeholder(R.drawable.verified_user_24)
                                            .error(R.drawable.verified_user_24)
                                            .into(binding.callerProfileIV);
                                } else {
                                    binding.callerProfileIV.setImageResource(R.drawable.verified_user_24);
                                }

                                if (callerInfo.has("isSpamCall")) {
                                    isSpamCall = callerInfo.getBoolean("isSpamCall");
                                }

                                if (isSpamCall) {
                                    binding.spamInfoTV.setVisibility(View.VISIBLE);
                                    binding.numberInfoCard.setBackgroundResource(R.drawable.background_caller_search_card_danger);

                                    String spamDescription = "☠️ Spam number";

                                    if (callerInfo.has("spamType")) {
                                        String spamType = callerInfo.getString("spamType");
                                        spamDescription += " (" + spamType + ")";
                                    }
                                    binding.spamInfoTV.setText(spamDescription);

                                    if (callerProfileImageLink.isEmpty()) {
                                        binding.callerProfileIV.setImageResource(R.drawable.error_outline_24);
                                    }
                                } else {
                                    binding.spamInfoTV.setVisibility(View.GONE);
                                    binding.numberInfoCard.setBackgroundResource(R.drawable.background_caller_search_card_normal);
                                }

                            } else {
                                binding.callerNameTV.setText(phoneNumber);
                                binding.phoneNumberTV.setText(phoneNumber);
                                binding.callerLocationTV.setText(CustomMethods.getCountryNameByCountryNameCode(countryISOCode));
                                binding.spamInfoTV.setVisibility(View.GONE);
                                Log.d(TAG, "onCreate: Cannot fetched number info.");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onCreate: ", e);
                            Toast.makeText(this, "JSON Exception", Toast.LENGTH_SHORT).show();
                            binding.inputBoxContainer.setVisibility(View.GONE);
                        }
                    });

                } else {
                    Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCreate: invalid phone number " + phoneNumber);
                    return false;
                }
                return true;
            } else {
                return false;
            }
        });
    }
}