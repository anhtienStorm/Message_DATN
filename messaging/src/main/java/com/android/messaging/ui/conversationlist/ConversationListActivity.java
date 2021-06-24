/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.ui.conversationlist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.SmsSecure;
import com.android.messaging.datamodel.action.DeleteAllMessageAction;
import com.android.messaging.datamodel.action.DeleteMessageAction;
import com.android.messaging.datamodel.action.SyncMessagesAction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.Trace;

public class ConversationListActivity extends AbstractConversationListActivity {

    private static final String MY_PREFS = "MyPrefs";
    private static final String ENCRYPT = "Encrypt";
    private static final String DECRYPT = "Decrypt";

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);
        Trace.endSection();
        invalidateActionBar();

        sharedPreferences = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();
        super.updateActionBar(actionBar);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Invalidate the menu as items that are based on settings may have changed
        // while not in the app (e.g. Talkback enabled/disable affects new conversation
        // button)
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (super.onCreateOptionsMenu(menu)) {
            return true;
        }
        getMenuInflater().inflate(R.menu.conversation_list_fragment_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_debug_options);
        if (item != null) {
            final boolean enableDebugItems = DebugUtils.isDebugEnabled();
            item.setVisible(enableDebugItems).setEnabled(enableDebugItems);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.action_start_new_conversation) {
            onActionBarStartNewConversation();
            return true;
        } else if (itemId == R.id.action_settings) {
            onActionBarSettings();
            return true;
        } else if (itemId == R.id.action_debug_options) {
            onActionBarDebug();
            return true;
        } else if (itemId == R.id.action_show_archived) {
            onActionBarArchived();
            return true;
        } else if (itemId == R.id.action_show_blocked_contacts) {
            onActionBarBlockedParticipants();
            return true;
        } else if (itemId == R.id.action_encrypt) {
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
            return true;
        } else if (itemId == R.id.action_decrypt) {
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
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onActionBarHome() {
        exitMultiSelectState();
    }

    public void onActionBarStartNewConversation() {
        UIIntents.get().launchCreateNewConversationActivity(this, null);
    }

    public void onActionBarSettings() {
        UIIntents.get().launchSettingsActivity(this);
    }

    public void onActionBarBlockedParticipants() {
        UIIntents.get().launchBlockedParticipantsActivity(this);
    }

    public void onActionBarArchived() {
        UIIntents.get().launchArchivedConversationsActivity(this);
    }

    @Override
    public boolean isSwipeAnimatable() {
        return !isInConversationListSelectMode();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final ConversationListFragment conversationListFragment =
                (ConversationListFragment) getFragmentManager().findFragmentById(
                        R.id.conversation_list_fragment);
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
        if (hasFocus && conversationListFragment != null) {
            conversationListFragment.setScrolledToNewestConversationIfNeeded();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String encryptAllMessage(String pass) {
        SmsSecure.generateIV();
        SmsSecure.generateSecretKey(pass);
        SmsSecure.generateSecretKeyS();

        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
                null, null, null);
        int d = 0;
        while (cursor.moveToNext()) {
            String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            String message_id = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
            if (body != null && !SmsSecure.isEncrypt(body)) {
                ContentValues values = new ContentValues();
                String body_encrypt = SmsSecure.encrypt(body);
                values.put(Telephony.Sms.BODY, SmsSecure.encryptS(body_encrypt));
                int numRowsUpdated = getContentResolver().update(Telephony.Sms.CONTENT_URI, values,
                        Telephony.Sms._ID + "=?",
                        new String[]{message_id});
                d += numRowsUpdated;
            }
        }
        if (d != 0) {
            DeleteAllMessageAction.deleteAllMessage();
            SyncMessagesAction.immediateSync();
            Log.d("TienNAb", "updateMessage: " + d);
            return "Done";
        } else {
            return "Tất cả các tin nhắn đều đã được mã hóa";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String decryptAllMessage(String pass) {
        SmsSecure.generateIV();
        SmsSecure.generateSecretKey(pass);
        SmsSecure.generateSecretKeyS();

        Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null,
                null, null, null);
        int d = 0;
        while (cursor.moveToNext()) {
            String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            String message_id = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
            if (body == null) {
                continue;
            }
            String smsDecrypt = SmsSecure.decryptS(body);

            if (smsDecrypt != null) {
                String decryptBody = SmsSecure.decrypt(smsDecrypt);
                if (decryptBody != null) {
                    ContentValues values = new ContentValues();
                    values.put(Telephony.Sms.BODY, SmsSecure.decrypt(smsDecrypt));
                    int numRowsUpdated = getContentResolver().update(Telephony.Sms.CONTENT_URI, values,
                            Telephony.Sms._ID + "=?",
                            new String[]{message_id});
                    d += numRowsUpdated;
                }
            }
        }
        if (d != 0) {
            DeleteAllMessageAction.deleteAllMessage();
            SyncMessagesAction.immediateSync();
            Log.d("TienNAb", "updateMessage: " + d);
            return "Done";
        } else {
            return "Sai mật khẩu hoặc không có tin nhắn mã hóa nào";
        }
    }

    private class encryptDecryptAsyncTask extends AsyncTask<String, String, String> {

        Activity activity;
        ProgressDialog progressDialog;

        public encryptDecryptAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... strings) {
            publishProgress(strings[0]);
            String s = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ENCRYPT.equals(strings[0])) {
                    s = encryptAllMessage(strings[1]);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ENCRYPT, true);
                    editor.apply();
                } else if (DECRYPT.equals(strings[0])) {
                    s = decryptAllMessage(strings[1]);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(ENCRYPT, false);
                    editor.apply();
                }
            }
            return s;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
            View view = getLayoutInflater().inflate(R.layout.progress_dialog_layout, null);
            TextView textLoading = view.findViewById(R.id.text_loading);
            textLoading.setText(values[0] + "ing...");
            progressDialog.setContentView(view);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
        }
    }
}
