package zorro.dimyon.calleridentity.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallLogUtils {

    private static final String TAG = "MADARA";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Interface for callback to handle results on the main thread
    public interface CallLogsCallback {
        void onCallLogsRetrieved(JSONArray callLogs);
        void onError(Exception e);
    }

    // Run task on a background thread
    private static void runOnBackgroundThread(Runnable task) {
        executorService.execute(task);
    }

    // Get today's call logs
    public static void getTodaysCallLogs(Context context, CallLogsCallback callback) {
        runOnBackgroundThread(() -> {
            try {
                JSONArray callLogs = new JSONArray();
                ContentResolver cr = context.getContentResolver();
                long todayStart = getStartOfTodayInMillis();

                String[] projection = {
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.TYPE
                };

                String selection = CallLog.Calls.DATE + " >= ?";
                String[] selectionArgs = {String.valueOf(todayStart)};

                Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                        String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

                        String callDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(date)));

                        JSONObject callLog = new JSONObject();
                        callLog.put("contactName", ContactUtils.getContactNameByPhoneNumber(context, number));
                        callLog.put("number", number);
                        callLog.put("date", callDate);
                        callLog.put("duration", duration);
                        callLog.put("type", type);

                        callLogs.put(callLog);

                        Log.d(TAG, "Number: " + number + ", Date: " + callDate + ", Duration: " + duration + " seconds, Type: " + type);
                    }
                    cursor.close();
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onCallLogsRetrieved(callLogs));

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // Get yesterday's call logs
    public static void getYesterdaysCallLogs(Context context, CallLogsCallback callback) {
        runOnBackgroundThread(() -> {
            try {
                JSONArray callLogs = new JSONArray();
                ContentResolver cr = context.getContentResolver();
                long[] yesterdayStartEnd = getStartAndEndOfYesterdayInMillis();

                String[] projection = {
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.TYPE
                };

                String selection = CallLog.Calls.DATE + " >= ? AND " + CallLog.Calls.DATE + " < ?";
                String[] selectionArgs = {String.valueOf(yesterdayStartEnd[0]), String.valueOf(yesterdayStartEnd[1])};

                Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                        String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

                        String callDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(date)));

                        JSONObject callLog = new JSONObject();
                        callLog.put("contactName", ContactUtils.getContactNameByPhoneNumber(context, number));
                        callLog.put("number", number);
                        callLog.put("date", callDate);
                        callLog.put("duration", duration);
                        callLog.put("type", type);

                        callLogs.put(callLog);

                        Log.d(TAG, "Number: " + number + ", Date: " + callDate + ", Duration: " + duration + " seconds, Type: " + type);
                    }
                    cursor.close();
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onCallLogsRetrieved(callLogs));

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // Get older call logs
    public static void getOlderCallLogs(Context context, CallLogsCallback callback) {
        runOnBackgroundThread(() -> {
            try {
                JSONArray callLogs = new JSONArray();
                ContentResolver cr = context.getContentResolver();
                long endOfYesterday = getEndOfYesterdayInMillis();

                String[] projection = {
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.TYPE
                };

                String selection = CallLog.Calls.DATE + " < ?";
                String[] selectionArgs = {String.valueOf(endOfYesterday)};

                Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);

                if (cursor != null) {

                    int i = 0;

                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                        String duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));

                        String callDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(date)));

                        JSONObject callLog = new JSONObject();
                        callLog.put("contactName", ContactUtils.getContactNameByPhoneNumber(context, number));
                        callLog.put("number", number);
                        callLog.put("date", callDate);
                        callLog.put("duration", duration);
                        callLog.put("type", type);

                        callLogs.put(callLog);

                        i++;

                        if (i == 100) {
                            break;
                        }
                        Log.d(TAG, "getOlderCallLogs: " + i);
                    }
                    cursor.close();
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onCallLogsRetrieved(callLogs));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // Helper method to get the start of today in milliseconds
    private static long getStartOfTodayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // Helper method to get the start and end of yesterday in milliseconds
    private static long[] getStartAndEndOfYesterdayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        long startOfYesterday = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfYesterday = calendar.getTimeInMillis();

        return new long[]{startOfYesterday, endOfYesterday};
    }

    // Helper method to get the end of yesterday in milliseconds
    private static long getEndOfYesterdayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTimeInMillis();
    }
}
