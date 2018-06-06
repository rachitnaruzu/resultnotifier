package com.resultnotifier.main.service;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

class CustomRequest extends Request<JSONObject> {

    private final Listener<JSONObject> mListener;
    private final Map<String, String> mParams;

    CustomRequest(int method, String url, Map<String, String> mParams,
                         Listener<JSONObject> responseListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = responseListener;
        this.mParams = mParams;
    }

    @Override
    public Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return mParams;
    }

    @Override
    public Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    public void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }
}