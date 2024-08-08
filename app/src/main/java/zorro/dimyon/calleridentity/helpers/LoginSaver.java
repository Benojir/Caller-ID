package zorro.dimyon.calleridentity.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginSaver {

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public LoginSaver(Context context) {
        preferences = context.getSharedPreferences("login_data", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void saveLogin(String apiKey) {
        editor.putString("api_key", apiKey);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    public String getApiKey() {
        return preferences.getString("api_key", "");
    }
}
