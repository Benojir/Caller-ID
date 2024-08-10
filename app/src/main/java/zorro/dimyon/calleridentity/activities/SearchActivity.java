package zorro.dimyon.calleridentity.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import zorro.dimyon.calleridentity.databinding.ActivitySearchBinding;
import zorro.dimyon.calleridentity.helpers.CallsControlHelper;
import zorro.dimyon.calleridentity.helpers.CustomMethods;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.inputPhoneNumberET.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.inputPhoneNumberET.getText().toString().isEmpty()) {

                String phoneNumber = binding.inputPhoneNumberET.getText().toString().trim();

                if (CustomMethods.isValidPhoneNumber(phoneNumber)) {

                    int countryCode = CustomMethods.getCountryCode(this, phoneNumber);

                    if (countryCode == -1) {
                        Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    String countryNameCode = CustomMethods.getCountryNameByCode(String.valueOf(countryCode));

                    CallsControlHelper controlHelper = new CallsControlHelper(this, phoneNumber, countryNameCode);

                    controlHelper.getCallerInfo(callerInfo -> {

                        if (callerInfo != null) {

                        } else {
                            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
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