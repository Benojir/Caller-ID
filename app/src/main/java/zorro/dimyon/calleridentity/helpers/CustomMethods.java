package zorro.dimyon.calleridentity.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import zorro.dimyon.calleridentity.R;

public class CustomMethods {

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

    public static String getCountryNameByCode(String countryCode) {
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry();
    }
}
