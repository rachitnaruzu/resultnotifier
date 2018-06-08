package com.resultnotifier.main.notification;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class FCMTokenUpdaterService extends IntentService {
    private static final String TAG = "REN_RENInstanceIdTokenU";
    private static final String BACKGROUND_THREAD_NAME = "token_updater_thread";

    public FCMTokenUpdaterService() {
        super(BACKGROUND_THREAD_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "Received request to update token");
        final String token = FirebaseInstanceId.getInstance().getToken();
        updateToken(token);
    }

    @WorkerThread
    public void updateToken(final String token) {
        Log.i(TAG, "Updating token=" + token);
    }
}
