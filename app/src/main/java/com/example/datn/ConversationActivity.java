package com.example.datn;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn.encrypt.SmsSecure;

import java.util.ArrayList;

public class ConversationActivity extends AppCompatActivity {

    private static final String THREAD_ID = "thread_id";
    private static final String ADDRESS = "address";
    private static final String MY_PREFS = "MyPrefs";
    private static final String ENCRYPT = "Encrypt";

    private String thread_id, title;
    private ArrayList<Message> list_message = new ArrayList<>();
    private RecyclerView messageRecyclerView;
    private MessageListAdapter adapter;
    private ImageView bt_send, bt_unlock;
    private EditText input;
    private TextView titleView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        thread_id = getIntent().getStringExtra(THREAD_ID);
        title = getIntent().getStringExtra(ADDRESS);
        setTitle("");

        messageRecyclerView = findViewById(R.id.list_message);
        bt_send = findViewById(R.id.bt_send);
        input = findViewById(R.id.input_message);

        adapter = new MessageListAdapter(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.setAdapter(adapter);

        updateList();

        bt_send.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(input.getText().toString())) {
                send_message(title, input);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(actionBar.getDisplayOptions()
                | androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        View actionbarView = getLayoutInflater().inflate(R.layout.actionbar_custom_layout,null);
        bt_unlock = actionbarView.findViewById(R.id.unlock);
        boolean bool;
        if (sharedPreferences.contains(ENCRYPT)){
            bool = sharedPreferences.getBoolean(ENCRYPT, false);
        } else {
            bool = false;
        }
        bt_unlock.setVisibility(bool ? View.VISIBLE : View.GONE);
        titleView = actionbarView.findViewById(R.id.title);
        titleView.setText(title);
        bt_unlock.setOnClickListener(v -> {
            View inputPassLayout = getLayoutInflater().inflate(R.layout.input_password_layout, null);
            EditText inputPass = inputPassLayout.findViewById(R.id.input_password);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nhập vào mật khẩu")
                    .setView(inputPassLayout)
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                        new DecryptAsyncTask(this).execute(inputPass.getText().toString());
                    })
                    .setNegativeButton("Huỷ", (dialogInterface, i) -> {})
                    .create().show();
        });
        actionBar.setCustomView(actionbarView);
    }

    void updateList(){
        load_message(thread_id);
        adapter.updateListMessage(list_message);
    }

    private void load_message(String id) {
        list_message.clear();
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
                "thread_id = " + id, null, null);
        while (cursor.moveToNext()) {
            String _id = cursor.getString(cursor.getColumnIndex("_id"));
            String thread_id = cursor.getString(cursor.getColumnIndex("thread_id"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String date_sent = cursor.getString(cursor.getColumnIndex("date_sent"));
            String read = cursor.getString(cursor.getColumnIndex("read"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            String reply_path_present = cursor.getString(
                    cursor.getColumnIndex("reply_path_present"));
            String subject = cursor.getString(cursor.getColumnIndex("subject"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
            String service_center = cursor.getString(
                    cursor.getColumnIndex("service_center"));
            String locked = cursor.getString(cursor.getColumnIndex("locked"));
            String error_code = cursor.getString(cursor.getColumnIndex("error_code"));
            String seen = cursor.getString(cursor.getColumnIndex("seen"));

            Message message = new Message(_id, thread_id, date, date_sent, read, status, type,
                    reply_path_present, subject, body, service_center, locked, error_code, seen);

            list_message.add(message);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void unlock_thread(String pass){
        list_message.clear();
        SmsSecure.generateIV();
        SmsSecure.generateSecretKey(pass);
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
                "thread_id = " + thread_id, null, null);
        while (cursor.moveToNext()) {
            String _id = cursor.getString(cursor.getColumnIndex("_id"));
            String thread_id = cursor.getString(cursor.getColumnIndex("thread_id"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String date_sent = cursor.getString(cursor.getColumnIndex("date_sent"));
            String read = cursor.getString(cursor.getColumnIndex("read"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            String reply_path_present = cursor.getString(
                    cursor.getColumnIndex("reply_path_present"));
            String subject = cursor.getString(cursor.getColumnIndex("subject"));

            String body = cursor.getString(cursor.getColumnIndex("body"));
            String decryptBody = SmsSecure.decrypt(body);
            if (decryptBody != null){
                decryptBody = decryptBody.replace("encrypted_by_AT","");
            } else {
                decryptBody = body;
            }

            String service_center = cursor.getString(
                    cursor.getColumnIndex("service_center"));
            String locked = cursor.getString(cursor.getColumnIndex("locked"));
            String error_code = cursor.getString(cursor.getColumnIndex("error_code"));
            String seen = cursor.getString(cursor.getColumnIndex("seen"));

            Message message = new Message(_id, thread_id, date, date_sent, read, status, type,
                    reply_path_present, subject, decryptBody, service_center, locked, error_code, seen);

            list_message.add(message);
        }
    }

    private void send_message(String destinationAddress, EditText smsEditText){
        // Set the service center address if needed, otherwise null.
        String scAddress = null;
        // Set pending intents to broadcast
        // when message sent and when delivered, or set to null.
        PendingIntent sentIntent = null, deliveryIntent = null;
        // Use SmsManager.
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(destinationAddress, scAddress, smsEditText.getText().toString(),
                        sentIntent, deliveryIntent);

//        Message message = new Message(null, thread_id, null, null, "1",
//                null, "2", null, null,
//                smsEditText.getText().toString(), null, null, null,
//                null, null);
//        list_message.add(message);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
                updateList();
                smsEditText.setText("");
//            }
//        }, 5000);
    }

    private class DecryptAsyncTask extends AsyncTask<String, Void, Void>{

        Activity activity;
        ProgressDialog progressDialog;

        public DecryptAsyncTask(Activity activity){
            this.activity = activity;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(String... strings) {
            publishProgress();
            unlock_thread(strings[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
            View view = activity.getLayoutInflater().inflate(R.layout.progress_dialog_layout, null);
            TextView textLoading = view.findViewById(R.id.text_loading);
            textLoading.setText("Loading...");
            progressDialog.setContentView(view);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
            Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
        }
    }
}
