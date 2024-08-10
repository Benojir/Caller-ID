package zorro.dimyon.calleridentity.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zorro.dimyon.calleridentity.R;

public class CallLogsAdapter extends RecyclerView.Adapter<CallLogsAdapter.MyViewHolder> {

    private static final String TAG = "MADARA";
    private final Activity activity;
    private final JSONArray callLogs;

    public CallLogsAdapter(Activity activity, JSONArray callLogs) {
        this.activity = activity;
        this.callLogs = callLogs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.sample_calllog_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        // Bind data to the views in the item layout here
        try {
            JSONObject callLogData = callLogs.getJSONObject(position);

            String contactName = callLogData.getString("contactName");
            String number = callLogData.getString("number");
            String date = callLogData.getString("date");
//            String duration = callLogData.getString("duration");
            int type = callLogData.getInt("type");

            if (contactName.isEmpty()) {
                contactName = number;
            }

            holder.contactNameTV.setText(contactName);

            holder.callLogTypeAndTimeTV.setCompoundDrawablePadding(10);
            holder.callLogTypeAndTimeTV.setText(date);

            if (type == CallLog.Calls.INCOMING_TYPE) {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.call_received_24, 0, 0, 0);
            } else if (type == CallLog.Calls.OUTGOING_TYPE) {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.call_made_24, 0, 0, 0);
            } else if (type == CallLog.Calls.MISSED_TYPE) {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.call_missed_24, 0, 0, 0);
            } else if (type == CallLog.Calls.REJECTED_TYPE) {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.call_received_24, 0, 0, 0);
            } else if (type == CallLog.Calls.BLOCKED_TYPE) {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.block_24, 0, 0, 0);
            } else if (type == CallLog.Calls.VOICEMAIL_TYPE) {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.voice_chat_24, 0, 0, 0);
            } else {
                holder.callLogTypeAndTimeTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.call_received_24, 0, 0, 0);
            }

            holder.callBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + number));
                activity.startActivity(intent);
            });

            holder.contactNameAndCallLogContainer.setOnClickListener(v -> {
                if (holder.hiddenViewsContainer.getVisibility() == View.GONE) {
                    holder.hiddenViewsContainer.setVisibility(View.VISIBLE);
                } else {
                    holder.hiddenViewsContainer.setVisibility(View.GONE);
                }
            });

            holder.messageBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + number));
                activity.startActivity(intent);
            });

            holder.historyBtn.setOnClickListener(v -> {
                Toast.makeText(activity, "Show history dialog here", Toast.LENGTH_SHORT).show();
                // Implement the logic to show the history dialog here
            });

        } catch (JSONException e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return callLogs.length();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout contactNameAndCallLogContainer;
        private final LinearLayout hiddenViewsContainer;
        private final LinearLayout messageBtn;
        private final LinearLayout historyBtn;
        private final TextView contactNameTV;
        private final TextView callLogTypeAndTimeTV;
        private final ImageButton callBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views in the item layout here
            // Define views in the item layout here
//            ImageView contactProfileIV = itemView.findViewById(R.id.contactProfileIV);
            contactNameAndCallLogContainer = itemView.findViewById(R.id.contactNameAndCallLogContainer);
            hiddenViewsContainer = itemView.findViewById(R.id.hiddenViewsContainer);
            messageBtn = itemView.findViewById(R.id.messageBtn);
            historyBtn = itemView.findViewById(R.id.historyBtn);
            contactNameTV = itemView.findViewById(R.id.contactNameTV);
            callLogTypeAndTimeTV = itemView.findViewById(R.id.callLogTypeAndTimeTV);
            callBtn = itemView.findViewById(R.id.callBtn);
        }
    }
}
