package zorro.dimyon.calleridentity.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import java.util.Locale;

public class CustomMethods {

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Remove country code for validation
        String nationalNumber = phoneNumber.replaceAll("\\D", "");

        // Check if the number is numeric and within the desired length
        return nationalNumber.matches("\\d{7,13}");
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getCountryCodeFromLocale(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        return locale.getCountry();
    }
}
