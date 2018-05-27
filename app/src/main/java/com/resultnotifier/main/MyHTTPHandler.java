package com.resultnotifier.main;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyHTTPHandler {
    private static MyHTTPHandler mMyHttpHandler;
    private final RequestQueue mRequestQueue;

    private MyHTTPHandler(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized MyHTTPHandler getInstance(Context context) {
        if (mMyHttpHandler == null) {
            mMyHttpHandler = new MyHTTPHandler(context);
        }
        return mMyHttpHandler;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}