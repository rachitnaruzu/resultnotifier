package com.resultnotifier.main.service;

import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.resultnotifier.main.Config;
import com.resultnotifier.main.FileData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.resultnotifier.main.Config.DOMAIN;

public class RENServiceClientImpl implements RENServiceClient {
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final String TAG = "REN_RENServiceClientIm";
    private static final String PUBLISHED_URL = DOMAIN + "/published/";
    private static final String TOPMOST_URL = DOMAIN + "/topmost/";
    private static final String FETCH_VIEWS_URL = DOMAIN + "/updateselfviews/";
    private static final String INCREMENT_VIEW_URL = DOMAIN + "/incrementviews/";
    private static final String FETCH_DATA_TYPES = DOMAIN + "/datatypes/";
    private static final String RECENT_URL = DOMAIN + "/recent/";
    private static final String TOKEN_UPDATE_URL = DOMAIN + "/register/";
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private MyHTTPHandler mMyHttpHandler;

    public RENServiceClientImpl(final MyHTTPHandler myHTTPHandler) {
        mMyHttpHandler = myHTTPHandler;
    }

    @Override
    public void updateSelfViews(final ArrayList<FileData> fileDataItems) {
        Log.i(TAG, "Updating self views");

        final String selfViews = getJSONSelfViews(fileDataItems);
        final Map<String, String> params = getSecureParams();
        params.put("selfviews", selfViews);
        if (selfViews.equals("{}")) {
            Log.i(TAG, "Not updating views as there are no self views");
        } else {
            final CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST,
                    FETCH_VIEWS_URL, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                Log.i("Update ", jsonObject.toString());
                            } catch (Exception e) {
                                VolleyLog.v("Update Error", jsonObject.toString());
                                e.printStackTrace();
                                Log.e("Update Error", jsonObject.toString());

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e(TAG, "Unable to update views", error);
                }
            });

            mMyHttpHandler.addToRequestQueue(updateSelfViewsRequest);
        }
    }

    @Override
    public void fetchPublishedFiles(final int offset, final String dataType,
                                    final FetchFilesCallback fetchFilesCallback) {
        Log.i(TAG, "Fetching published files. offset=" + offset + "; data type=" + dataType);
        fetchFiles(offset, dataType, PUBLISHED_URL, fetchFilesCallback);
    }

    @Override
    public void fetchRecentFiles(final int offset, final String dataType,
                                 final FetchFilesCallback fetchFilesCallback) {
        Log.i(TAG, "Fetching recent files. offset=" + offset + "; data type=" + dataType);
        fetchFiles(offset, dataType, RECENT_URL, fetchFilesCallback);
    }

    @Override
    public void fetchTopMostFiles(final int offset, final String dataType,
                                  final FetchFilesCallback fetchFilesCallback) {
        Log.i(TAG, "Fetching top most files. offset=" + offset + "; data type=" + dataType);
        fetchFiles(offset, dataType, TOPMOST_URL, fetchFilesCallback);
    }

    @Override
    public void incrementViewsByOne(final String fileId,
                                    final IncrementViewsCallback incrementViewsCallback) {
        Log.i(TAG, "Incrementing the views by one for file ID=" + fileId);
        final Map<String, String> params = getSecureParams();
        params.put("fileid", fileId);
        final CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST,
                INCREMENT_VIEW_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.i(TAG, "Successfully updated views by one. file ID=" + fileId
                                + "; response=" + response);
                        incrementViewsCallback.onSuccess();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.e(TAG, "Unable to update views by one", error);
                incrementViewsCallback.onError(HTTP_INTERNAL_SERVER_ERROR);
            }
        });

        mMyHttpHandler.addToRequestQueue(updateSelfViewsRequest);
    }

    @Override
    public void fetchDataTypes(final FetchDataTypesCallback fetchDataTypesCallback) {
        Log.i(TAG, "Fetching data types");

        final Response.Listener<JSONObject> successResponseListener =
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.i(TAG, "Successfully fetched data types. response=" + response);
                        try {
                            final JSONArray jArray = response.getJSONArray("datatypes");
                            final List<String> dataTypes = new ArrayList<>(jArray.length());
                            for (int i = 0; i < jArray.length(); i++) {
                                dataTypes.add(jArray.getString(i));
                            }
                            fetchDataTypesCallback.onSuccess(dataTypes);
                        } catch (final JSONException e) {
                            Log.e(TAG, "Unable to parse data types.", e);
                            fetchDataTypesCallback.onError(HTTP_INTERNAL_SERVER_ERROR);
                        }
                    }
                };

        final Response.ErrorListener errorResponseListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.e(TAG, "Unable to fetch data types.", error);
                fetchDataTypesCallback.onError(HTTP_INTERNAL_SERVER_ERROR);
            }
        };

        final Map<String, String> params = getSecureParams();
        final CustomRequest fetchRequest = new CustomRequest(Request.Method.POST,
                FETCH_DATA_TYPES, params, successResponseListener,
                errorResponseListener);

        mMyHttpHandler.addToRequestQueue(fetchRequest);
    }

    @Override
    public void updateToken(final String token, final UpdateTokenCallback updateTokenCallback) {
        Log.i(TAG, "Updating token=" + token);

        final Response.Listener<JSONObject> successResponseListener =
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.i(TAG, "Successfully updated tokens. response=" + response);
                        updateTokenCallback.onSuccess();
                    }
                };

        final Response.ErrorListener errorResponseListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.e(TAG, "Unable to update token=" + token, error);
                updateTokenCallback.onError(HTTP_INTERNAL_SERVER_ERROR);
            }
        };

        final Map<String, String> params = getSecureParams();
        params.put("registration_id", token);
        final CustomRequest fetchRequest = new CustomRequest(Request.Method.POST,
                TOKEN_UPDATE_URL, params, successResponseListener,
                errorResponseListener);

        mMyHttpHandler.addToRequestQueue(fetchRequest);
    }

    private void fetchFiles(final int offset, final String dataType, final String url,
                            final FetchFilesCallback fetchFilesCallback) {
        final Map<String, String> params = getSecureParams();
        params.put("offset", String.valueOf(offset));
        params.put("datatypes", dataType);
        final CustomRequest fetchRequest = new CustomRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.i(TAG, "Successfully received files response; response="
                                + response);
                        try {
                            final JSONArray jArray = response.getJSONArray("files");
                            final List<FileData> files = new ArrayList<>(jArray.length());
                            for (int i = 0; i < jArray.length(); i++) {
                                final JSONObject oneObject = jArray.getJSONObject(i);
                                final FileData fileData = new FileData();
                                fileData.setDateCreated(oneObject.getString("datecreated"));
                                fileData.setDisplayName(oneObject.getString("displayname"));
                                fileData.setUrl(oneObject.getString("url"));
                                fileData.setDataType(oneObject.getString("datatype"));
                                fileData.setViews(oneObject.getString("views"));
                                fileData.setFileType(oneObject.getString("filetype"));
                                fileData.setFileId(oneObject.getString("fileid"));
                                files.add(fileData);
                            }
                            fetchFilesCallback.onSuccess(files);
                        } catch (final JSONException e) {
                            Log.i(TAG, "Unable to get data from response.", e);
                            fetchFilesCallback.onError(HTTP_INTERNAL_SERVER_ERROR);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.i(TAG, "Unable to receive files response", error);
                fetchFilesCallback.onError(HTTP_INTERNAL_SERVER_ERROR);
            }
        });

        mMyHttpHandler.addToRequestQueue(fetchRequest);
    }

    private String getJSONSelfViews(final ArrayList<FileData> fileDataItems) {
        final StringBuilder selfViewsB = new StringBuilder("[");
        boolean is_any_one_self_view = false;
        for (final FileData fileData : fileDataItems) {
            if (!fileData.getSelfViews().equals("0")) {
                is_any_one_self_view = true;
                selfViewsB.append("{\"mFileId\":\"" + fileData.getFileId() + "\",\"selfviews\":\""
                        + fileData.getSelfViews() + "\"}" + ",");
            }
        }
        selfViewsB.replace(selfViewsB.length() - 1, selfViewsB.length(), "]");
        String selfViews = "[]";
        if (is_any_one_self_view) selfViews = selfViewsB.toString();
        return selfViews;
    }

    private Map<String, String> getSecureParams() {
        Map<String, String> params = new HashMap<>();
        return addSecureParams(params);
    }

    private Map<String, String> addSecureParams(Map<String, String> params) {
        String ts = getEncodedTimestamp();
        params.put("sec", ts);
        params.put("sig", encodeStringData(Config.SECRET_KEY, ts));
        return params;
    }

    private String encodeStringData(String key, String value) {
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            Log.e("MainFragment", e.getMessage());
        }
        return null;
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String getEncodedTimestamp() {
        Calendar currentTime = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String ts = dateFormat.format(currentTime.getTime());
        return Base64.encodeToString(ts.getBytes(), Base64.URL_SAFE);
    }
}
