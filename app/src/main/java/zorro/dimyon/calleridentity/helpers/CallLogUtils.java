package zorro.dimyon.calleridentity.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CallLogUtils {

    private static final String TAG = "MADARA";

//    -----------------------------Get today call logs----------------------------------------------

    public static JSONArray getTodaysCallLogs(Context context) throws JSONException {
        JSONArray callLogs = new JSONArray();
        ContentResolver cr = context.getContentResolver();

        // Get today's date in milliseconds
        long todayStart = getStartOfTodayInMillis();

        // Specify the columns you want to retrieve
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
        };

        // Define the selection criteria
        String selection = CallLog.Calls.DATE + ">= ?";
        String[] selectionArgs = { String.valueOf(todayStart) };

        // Query the call log content provider
        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);

        // Process the results
        while (cursor != null && cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

            // Convert the date from milliseconds to a readable format
            String callDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(date)));

            // Add the call log to the JSONObject
            JSONObject callLog = new JSONObject();
            callLog.put("contactName", ContactUtils.getContactNameByPhoneNumber(context, number));
            callLog.put("number", number);
            callLog.put("date", callDate);
            callLog.put("duration", duration);
            callLog.put("type", type);

            // Add the call log to the JSONArray
            callLogs.put(callLog);

            Log.d(TAG, "Number: " + number + ", Date: " + callDate + ", Duration: " + duration + " seconds, Type: " + type);
        }

        if (cursor != null) {
            cursor.close();
        }
        return callLogs;
    }

    private static long getStartOfTodayInMillis() {
        // Get the current time and reset to midnight
        Date today = new Date();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        return today.getTime();
    }

//    ---------------------------------Get yesterday call logs--------------------------------------

    public static JSONArray getYesterdaysCallLogs(Context context) throws JSONException {
        JSONArray callLogs = new JSONArray();
        ContentResolver cr = context.getContentResolver();

        // Get yesterday's start and end date in milliseconds
        long[] yesterdayStartEnd = getStartAndEndOfYesterdayInMillis();

        // Specify the columns you want to retrieve
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
        };

        // Define the selection criteria
        String selection = CallLog.Calls.DATE + ">= ? AND " + CallLog.Calls.DATE + " < ?";
        String[] selectionArgs = { String.valueOf(yesterdayStartEnd[0]), String.valueOf(yesterdayStartEnd[1]) };

        // Query the call log content provider
        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);

        // Process the results
        while (cursor != null && cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

            // Convert the date from milliseconds to a readable format
            String callDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(date)));

            // Add the call log to the JSONObject
            JSONObject callLog = new JSONObject();
            callLog.put("contactName", ContactUtils.getContactNameByPhoneNumber(context, number));
            callLog.put("number", number);
            callLog.put("date", callDate);
            callLog.put("duration", duration);
            callLog.put("type", type);

            // Add the call log to the JSONArray
            callLogs.put(callLog);

            Log.d(TAG, "Number: " + number + ", Date: " + callDate + ", Duration: " + duration + " seconds, Type: " + type);
        }

        if (cursor != null) {
            cursor.close();
        }
        return callLogs;
    }

    // Helper method to get the start and end of yesterday in milliseconds
    private static long[] getStartAndEndOfYesterdayInMillis() {
        Calendar calendar = Calendar.getInstance();

        // Set to the start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Subtract one day to get the start of yesterday
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        long startOfYesterday = calendar.getTimeInMillis();

        // Add 24 hours to get the end of yesterday
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfYesterday = calendar.getTimeInMillis();

        return new long[]{startOfYesterday, endOfYesterday};
    }

//    -----------------------------Get older call logs----------------------------------------------

    public static JSONArray getOlderCallLogs(Context context) throws JSONException {
        JSONArray callLogs = new JSONArray();
        ContentResolver cr = context.getContentResolver();

        // Get the end of yesterday in milliseconds
        long endOfYesterday = getEndOfYesterdayInMillis();

        // Specify the columns you want to retrieve
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
        };

        // Define the selection criteria for logs older than yesterday
        String selection = CallLog.Calls.DATE + " < ?";
        String[] selectionArgs = { String.valueOf(endOfYesterday) };

        // Query the call log content provider
        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);

        // Process the results
        while (cursor != null && cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

            // Convert the date from milliseconds to a readable format
            String callDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(date)));

            // Add the call log to the JSONObject
            JSONObject callLog = new JSONObject();
            callLog.put("contactName", ContactUtils.getContactNameByPhoneNumber(context, number));
            callLog.put("number", number);
            callLog.put("date", callDate);
            callLog.put("duration", duration);
            callLog.put("type", type);

            // Add the call log to the JSONArray
            callLogs.put(callLog);

            Log.d(TAG, "Number: " + number + ", Date: " + callDate + ", Duration: " + duration + " seconds, Type: " + type);
        }

        if (cursor != null) {
            cursor.close();
        }
        return callLogs;
    }

    // Helper method to get the end of yesterday in milliseconds
    private static long getEndOfYesterdayInMillis() {
        Calendar calendar = Calendar.getInstance();

        // Set to the start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Subtract one millisecond to get the end of yesterday
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTimeInMillis();
    }

}
