package com.resultnotifier.main.notification;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.resultnotifier.main.AppState;
import com.resultnotifier.main.service.RENServiceClient;

import java.util.concurrent.CountDownLatch;

import static com.resultnotifier.main.SharedPreferencesKeys.TOKEN;

public class FCMTokenUpdaterService extends IntentService {
    private static final String TAG = "REN_RENInstanceIdTokenU";
    private static final String BACKGROUND_THREAD_NAME = "token_updater_thread";

    private SharedPreferences mSharedPreferences;
    private RENServiceClient mRenServiceClient;

    public FCMTokenUpdaterService() {
        super(BACKGROUND_THREAD_NAME);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Creating fcm token updater service");
        final Context applicationContext = getApplicationContext();
        mSharedPreferences = AppState.getSharedPreferences(applicationContext);
        mRenServiceClient = AppState.getRenServiceClient(applicationContext);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying fcm token updater service");
        super.onDestroy();
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

        final String savedToken = mSharedPreferences.getString(TOKEN, null);
        if (savedToken != null && savedToken.equals(token)) {
            Log.i(TAG, "Token did not get change from the last time, ignoring update request");
            return;
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        mRenServiceClient.updateToken(token, new RENServiceClient.UpdateTokenCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Successfully updated the token=" + token);
                mSharedPreferences.edit().putString(TOKEN, token).apply();
                countDownLatch.countDown();
            }

            @Override
            public void onError(final int error) {
                Log.e(TAG, "Failed to update token=" + token + "; error=" + error);
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (final InterruptedException e) {
            Log.e(TAG, "Thread interrupted while waiting for the response " +
                    "for token update. token=" + token, e);
        }
    }
}
