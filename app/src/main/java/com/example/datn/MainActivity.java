package com.example.datn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.role.RoleManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datn.encrypt.SmsSecure;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SMS = 1;
    private static final int SET_AS_DEFAULT_INTENT = 2;
    private static final String MY_PREFS = "MyPrefs";
    private static final String ENCRYPT = "Encrypt";
    private static final String DECRYPT = "Decrypt";

    ArrayList<Conversation> list_conversation = new ArrayList<>();
    RecyclerView conversationRecyclerview;
    ConversationsListAdapter adapter;
    SharedPreferences sharedPreferences;
    LinearLayout setSMSAppDefaultView;
    Button setSMSAppDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Tin Nhắn");
        conversationRecyclerview = findViewById(R.id.list_conversation);
        setSMSAppDefaultView = findViewById(R.id.set_sms_app_default_view);
        setSMSAppDefault = findViewById(R.id.bt_set_sms_app_default);
        setSMSAppDefault.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                RoleManager roleManager = getSystemService(RoleManager.class);
                Intent roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS);
                startActivityForResult(roleRequestIntent, SET_AS_DEFAULT_INTENT);
            } else {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, SET_AS_DEFAULT_INTENT);
            }
        });

        adapter = new ConversationsListAdapter(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        conversationRecyclerview.setAdapter(adapter);
        conversationRecyclerview.setLayoutManager(linearLayoutManager);

        checkForSmsPermission();
        sharedPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSMSAppDefault();
        adapter.updateConversationList(list_conversation);
    }

    private void checkSMSAppDefault() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            boolean isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS);
            if (isRoleAvailable) {
                boolean isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS);
                if (!isRoleHeld) {
                    conversationRecyclerview.setVisibility(View.GONE);
                    setSMSAppDefaultView.setVisibility(View.VISIBLE);
                } else {
                    conversationRecyclerview.setVisibility(View.VISIBLE);
                    setSMSAppDefaultView.setVisibility(View.GONE);
                }
            }
        } else {
            String currentDefault = Telephony.Sms.getDefaultSmsPackage(this);
            boolean isDefault = getPackageName().equals(currentDefault);
            if (!isDefault) {
                conversationRecyclerview.setVisibility(View.GONE);
                setSMSAppDefaultView.setVisibility(View.VISIBLE);
            } else {
                conversationRecyclerview.setVisibility(View.VISIBLE);
                setSMSAppDefaultView.setVisibility(View.GONE);
            }
        }
    }

    private void load_list_message() {
        list_conversation.clear();
        Uri uri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI;
        Cursor cursor = getContentResolver().query(uri, null, null,
                null, Telephony.Sms.DATE+" DESC");
        while (cursor.moveToNext()) {
            String date = cursor.getString(
                    cursor.getColumnIndex("date"));
            String reply_path_present = cursor.getString(
                    cursor.getColumnIndex("reply_path_present"));
            String body = cursor.getString(
                    cursor.getColumnIndex("body"));
            String type = cursor.getString(
                    cursor.getColumnIndex("type"));
            String thread_id = cursor.getString(
                    cursor.getColumnIndex("thread_id"));
            String locked = cursor.getString(
                    cursor.getColumnIndex("locked"));
            String date_sent = cursor.getString(
                    cursor.getColumnIndex("date_sent"));
            String read = cursor.getString(
                    cursor.getColumnIndex("read"));
            String address = cursor.getString(
                    cursor.getColumnIndex("address"));
            String service_center = cursor.getString(
                    cursor.getColumnIndex("service_center"));
            String error_code = cursor.getString(
                    cursor.getColumnIndex("error_code"));
            String _id = cursor.getString(
                    cursor.getColumnIndex("_id"));
            String status = cursor.getString(
                    cursor.getColumnIndex("status"));

            Conversation conversation = new Conversation(date, reply_path_present, body, type,
                    thread_id, locked, date_sent, read, address, service_center,
                    error_code, _id, status);

            list_conversation.add(conversation);
        }
        adapter.notifyDataSetChanged();
    }

    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not yet granted. Use requestPermissions().
            // MY_PERMISSIONS_REQUEST_SEND_SMS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_REQUEST_SMS);
        } else {
            // Permission already granted. Enable the SMS button.
            load_list_message();
            adapter.updateConversationList(list_conversation);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SMS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    load_list_message();
                    adapter.updateConversationList(list_conversation);
                } else {
                    // Permission denied.
                    Toast.makeText(this, "failure_permission", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SET_AS_DEFAULT_INTENT:
                if (resultCode == -1)
                    Toast.makeText(this, "Ứng dụng đã được đặt làm mặc định", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.encrypt_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean bool;
        if (sharedPreferences.contains(ENCRYPT)) {
            bool = sharedPreferences.getBoolean(ENCRYPT, false);
        } else {
            bool = false;
        }
        menu.findItem(R.id.encrypt_message).setVisible(!bool);
        menu.findItem(R.id.decrypt_message).setVisible(bool);
        return super.onPrepareOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.encrypt_message:
                View inputPassEncryptLayout = getLayoutInflater().inflate(R.layout.input_password_layout, null);
                EditText inputPassEncrypt = inputPassEncryptLayout.findViewById(R.id.input_password);
                AlertDialog.Builder encryptDialogBuilder = new AlertDialog.Builder(this);
                encryptDialogBuilder.setTitle("Nhập vào mật khẩu")
                        .setView(inputPassEncryptLayout)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            new encryptDecryptAsyncTask(this).execute(
                                    ENCRYPT, inputPassEncrypt.getText().toString());
                        })
                        .setNegativeButton("Huỷ", (dialogInterface, i) -> {
                        });
                AlertDialog EncryptAlertDialog = encryptDialogBuilder.create();
                EncryptAlertDialog.setCanceledOnTouchOutside(false);
                EncryptAlertDialog.show();
                break;
            case R.id.decrypt_message:
                View inputPassDecryptLayout = getLayoutInflater().inflate(R.layout.input_password_layout, null);
                EditText inputPassDecrypt = inputPassDecryptLayout.findViewById(R.id.input_password);
                AlertDialog.Builder decryptDialogBuilder = new AlertDialog.Builder(this);
                decryptDialogBuilder.setTitle("Nhập vào mật khẩu")
                        .setView(inputPassDecryptLayout)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            new encryptDecryptAsyncTask(this).execute(
                                    DECRYPT, inputPassDecrypt.getText().toString());
                        })
                        .setNegativeButton("Huỷ", (dialogInterface, i) -> {
                        });
                AlertDialog DecryptAlertDialog = decryptDialogBuilder.create();
                DecryptAlertDialog.setCanceledOnTouchOutside(false);
                DecryptAlertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void encryptAllMessage(String pass) {
        SmsSecure.generateIV();
        SmsSecure.generateSecretKey(pass);
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
                null, null, null);
        int d = 0;
        while (cursor.moveToNext()) {
            String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            ContentValues values = new ContentValues();
            values.put(Telephony.Sms.BODY, SmsSecure.encrypt("encrypted_by_AT" + body));
            int numRowsUpdated = getContentResolver().update(Telephony.Sms.CONTENT_URI, values,
                    Telephony.Sms._ID + "=?",
                    new String[]{String.valueOf(cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID)))});
            d += numRowsUpdated;
        }
        Log.d("TienNAb", "updateMessage: " + d);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void decryptAllMessage(String pass) {
        SmsSecure.generateIV();
        SmsSecure.generateSecretKey(pass);
        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
                null, null, null);
        int d = 0;
        while (cursor.moveToNext()) {
            String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            String decryptBody = SmsSecure.decrypt(body);
            ContentValues values = new ContentValues();
            if (decryptBody == null){
                values.put(Telephony.Sms.BODY, body);
            } else if (decryptBody.contains("encrypted_by_AT")) {
                values.put(Telephony.Sms.BODY, decryptBody.replace("encrypted_by_AT", ""));
            } else {
                values.put(Telephony.Sms.BODY, body);
            }
            int numRowsUpdated = getContentResolver().update(Telephony.Sms.CONTENT_URI, values,
                    Telephony.Sms._ID + "=?",
                    new String[]{String.valueOf(cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID)))});
            d += numRowsUpdated;
        }
        Log.d("TienNAb", "updateMessage: " + d);
    }

    private class encryptDecryptAsyncTask extends AsyncTask<String, String, Void> {

        Activity activity;
        ProgressDialog progressDialog;

        public encryptDecryptAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(String... strings) {
            publishProgress(strings[0]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ENCRYPT.equals(strings[0])) {
                    encryptAllMessage(strings[1]);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ENCRYPT, true);
                    editor.apply();
                } else if (DECRYPT.equals(strings[0])) {
                    decryptAllMessage(strings[1]);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ENCRYPT, false);
                    editor.apply();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
            View view = activity.getLayoutInflater().inflate(R.layout.progress_dialog_layout, null);
            TextView textLoading = view.findViewById(R.id.text_loading);
            textLoading.setText(values[0] + "ing...");
            progressDialog.setContentView(view);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
            load_list_message();
        }
    }
}