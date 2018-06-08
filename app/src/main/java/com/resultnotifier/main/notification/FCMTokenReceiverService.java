package com.resultnotifier.main.notification;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCMTokenReceiverService extends FirebaseInstanceIdService {
    private static final String TAG = "REN_FCMInstanceIdToken";

    @Override
    public void onTokenRefresh() {
        Log.i(TAG, "Token is refreshed");

        final Intent fcmTokeUpdaterServiceIntent =
                new Intent(this, FCMTokenUpdaterService.class);
        startService(fcmTokeUpdaterServiceIntent);
    }
}
