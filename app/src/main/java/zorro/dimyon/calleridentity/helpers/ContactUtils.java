package zorro.dimyon.calleridentity.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import androidx.core.content.ContextCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactUtils {

    public interface OnContactNameRetrievedListener {
        void onContactNameRetrieved(String contactName);
    }

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void getContactNameByPhoneNumber(Context context, String phoneNumber, OnContactNameRetrievedListener listener) {
        executorService.execute(() -> {
            String contactName = "";

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            String storedNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            if (PhoneNumberUtils.compare(storedNumber, phoneNumber)) {
                                contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                break; // Exit the loop as we found the contact
                            }
                        }
                    } finally {
                        cursor.close(); // Ensure the cursor is closed in case of an exception
                    }
                }
            }

            // Run the listener callback on the main thread
            final String result = contactName;
            ((android.app.Activity) context).runOnUiThread(() -> listener.onContactNameRetrieved(result));
        });
    }
}
