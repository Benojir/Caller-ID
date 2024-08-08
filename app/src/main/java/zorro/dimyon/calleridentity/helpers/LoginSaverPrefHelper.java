package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginSaverPrefHelper {

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public LoginSaverPrefHelper(Context context) {
        preferences = context.getSharedPreferences("login_data", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void saveApiKey(String apiKey) {
        editor.putString("api_key", apiKey);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    public String getApiKey() {
        return preferences.getString("api_key", "");
    }

//    ----------------------------------------------------------------------------------------------

    public void saveCountryNameCode(String countryNameCode) {
        editor.putString("country_name_code", countryNameCode);
        editor.apply();
    }

    public String getCountryNameCode() {
        return preferences.getString("country_name_code", "IN");
    }

//    ----------------------------------------------------------------------------------------------

    public void saveNumber(String number) {
        editor.putString("number", number);
        editor.apply();
    }

    public String getNumber() {
        return preferences.getString("number", "");
    }

//    ----------------------------------------------------------------------------------------------

    public void saveDialingCode(int dialingCode) {
        editor.putInt("dialing_code", dialingCode);
        editor.apply();
    }

    public int getDialingCode() {
        return preferences.getInt("dialing_code", 91);
    }

//    ----------------------------------------------------------------------------------------------

    public void saveOTPRequestId(String requestId) {
        editor.putString("otp_request_id", requestId);
        editor.apply();
    }

    public String getOTPRequestId() {
        return preferences.getString("otp_request_id", "");
    }
}
