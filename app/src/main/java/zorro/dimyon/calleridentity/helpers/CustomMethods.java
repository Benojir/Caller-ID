package zorro.dimyon.calleridentity.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import zorro.dimyon.calleridentity.R;

public class CustomMethods {

    private static final String TAG = "MADARA";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Remove country code for validation
        String nationalNumber = phoneNumber.replaceAll("\\D", "");

        // Check if the number is numeric and within the desired length
        return nationalNumber.matches("\\d{7,13}");
    }

    public static boolean isValidOTP(String otp) {

        boolean isValid = false;

        if (otp.length() >= 4 && otp.length() <= 10) {
            try {
                Integer.parseInt(otp);
                isValid = true;
            } catch (NumberFormatException ignored) {
            }
        }

        return isValid;
    }
//--------------------------------------------------------------------------------------------------

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

//    ----------------------------------------------------------------------------------------------

    public static boolean isGzipEncoded(byte[] bytes) {
        return bytes.length > 1 && bytes[0] == (byte) 0x1f && bytes[1] == (byte) 0x8b;
    }

    public static String decompressGzip(byte[] compressed) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        StringBuilder out = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) != -1) {
            out.append(new String(buffer, 0, len));
        }
        return out.toString();
    }

//    ----------------------------------------------------------------------------------------------

    public static void errorAlert(Activity activity, String errorTitle, String errorBody, String actionButton, boolean shouldGoBack) {

        if (!activity.isFinishing()){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(errorTitle);
            builder.setMessage(errorBody);
            builder.setIcon(R.drawable.warning_24);
            builder.setPositiveButton(actionButton, (dialogInterface, i) -> {
                if (shouldGoBack){
                    activity.finish();
                }
                else {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

//    ----------------------------------------------------------------------------------------------

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            if (capabilities != null) {
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        }

        return false;
    }
//    ----------------------------------------------------------------------------------------------

    public static String getCountryNameByCountryNameCode(String countryNameCode) {
        Locale locale = new Locale("", countryNameCode);
        return locale.getDisplayCountry();
    }

    // Method to get the country ISO code from the country code
    public static String getISOCodeByDialingCode(int countryCode) {
        // Create an instance of PhoneNumberUtil
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        // Get the region code (ISO country code) for the given country code
        String regionCode = phoneNumberUtil.getRegionCodeForCountryCode(countryCode);

        // If regionCode is null or empty, the country code is invalid
        if (regionCode == null || regionCode.isEmpty()) {
            return "IN"; // Handle invalid input appropriately
        }

        return regionCode; // Return the country ISO code (e.g., "IN", "US")
    }

//    ----------------------------------------------------------------------------------------------

    // Method to get the country code from a phone number
    public static int getCountryCode(Context context, String phoneNumber) {
        // Create an instance of PhoneNumberUtil
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        // Get the default country ISO code from the TelephonyManager
        String defaultRegion = getCountryIso(context);

        try {
            // Parse the phone number
            Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, defaultRegion);

            // Check if the phone number includes a country code
            if (parsedNumber.hasCountryCode()) {
                // Return the country code from the parsed number
                return parsedNumber.getCountryCode();
            } else {
                // Return the default country code for the user's region
                return phoneNumberUtil.getCountryCodeForRegion(defaultRegion);
            }

        } catch (NumberParseException e) {
            Log.e(TAG, "getCountryCode: ", e);
            return -1; // Return an error code or handle appropriately
        }
    }

    // Method to get the country ISO code from the TelephonyManager
    private static String getCountryIso(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = tm.getNetworkCountryIso().toUpperCase();

        if (countryIso.isEmpty()) {
            // Fallback to the SIM country if the network country is not available
            countryIso = tm.getSimCountryIso().toUpperCase();
        }

        return countryIso;
    }

//--------------------------------------------------------------------------------------------------

    public static void showKeyBoard(Activity activity, EditText editText) {
        if (editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

//--------------------------------------------------------------------------------------------------
}
