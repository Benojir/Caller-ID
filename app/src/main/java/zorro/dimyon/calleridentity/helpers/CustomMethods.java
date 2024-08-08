package zorro.dimyon.calleridentity.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

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
}
