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

package com.android.messaging.datamodel.action;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.LogUtil;

/**
 * Action used to delete a single message.
 */
public class DeleteAllMessageAction extends Action implements Parcelable {
    private static final String TAG = LogUtil.BUGLE_DATAMODEL_TAG;

    public static void deleteAllMessage() {
        final DeleteAllMessageAction action = new DeleteAllMessageAction();
        action.start();
    }

    private static final String KEY_MESSAGE_ID = "message_id";

    private DeleteAllMessageAction() {
        super();
    }

    // Doing this work in the background so that we're not competing with sync
    // which could bring the deleted message back to life between the time we deleted
    // it locally and deleted it in telephony (sync is also done on doBackgroundWork).
    //
    // Previously this block of code deleted from telephony first but that can be very
    // slow (on the order of seconds) so this was modified to first delete locally, trigger
    // the UI update, then delete from telephony.
    @Override
    protected Bundle doBackgroundWork() {
        final DatabaseWrapper db = DataModel.get().getDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                    new String[]{DatabaseHelper.MessageColumns._ID},
                    null,
                    null,
                    null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // First find the thread id for this conversation.
//                    final String messageId = actionParameters.getString(KEY_MESSAGE_ID);
                    final String messageId = cursor.getString(0);

                    if (!TextUtils.isEmpty(messageId)) {
                        // Check message still exists
                        final MessageData message = BugleDatabaseOperations.readMessage(db, messageId);
                        if (message != null) {
                            // Delete from local DB
                            int count = BugleDatabaseOperations.deleteMessage(db, messageId);
                            if (count > 0) {
                                LogUtil.i(TAG, "DeleteMessageAction: Deleted local message "
                                        + messageId);
                            } else {
                                LogUtil.w(TAG, "DeleteMessageAction: Could not delete local message "
                                        + messageId);
                            }
                            MessagingContentProvider.notifyMessagesChanged(message.getConversationId());
                            // We may have changed the conversation list
                            MessagingContentProvider.notifyConversationListChanged();

//                final Uri messageUri = message.getSmsMessageUri();
//                if (messageUri != null) {
//                    // Delete from telephony DB
//                    count = MmsUtils.deleteMessage(messageUri);
//                    if (count > 0) {
//                        LogUtil.i(TAG, "DeleteMessageAction: Deleted telephony message "
//                                + messageUri);
//                    } else {
//                        LogUtil.w(TAG, "DeleteMessageAction: Could not delete message from "
//                                + "telephony: messageId = " + messageId + ", telephony uri = "
//                                + messageUri);
//                    }
//                } else {
//                    LogUtil.i(TAG, "DeleteMessageAction: Local message " + messageId
//                            + " has no telephony uri.");
//                }
                        } else {
                            LogUtil.w(TAG, "DeleteMessageAction: Message " + messageId + " no longer exists");
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Delete the message.
     */
    @Override
    protected Object executeAction() {
        requestBackgroundWork();
        return null;
    }

    private DeleteAllMessageAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<DeleteAllMessageAction> CREATOR
            = new Parcelable.Creator<DeleteAllMessageAction>() {
        @Override
        public DeleteAllMessageAction createFromParcel(final Parcel in) {
            return new DeleteAllMessageAction(in);
        }

        @Override
        public DeleteAllMessageAction[] newArray(final int size) {
            return new DeleteAllMessageAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
