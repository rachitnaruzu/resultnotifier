package com.resultnotifier.main.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.resultnotifier.main.CommonUtility;
import com.resultnotifier.main.CustomRequest;
import com.resultnotifier.main.FileData;
import com.resultnotifier.main.MyHTTPHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.resultnotifier.main.CommonUtility.DOMAIN;

public class RENServiceClientImpl implements RENServiceClient {
    private static final String TAG = "REN_RENServiceClientIm";

    public static final String PUBLISHED_URL = DOMAIN + "/published/";
    public static final String TOPMOST_URL = DOMAIN + "/topmost/";
    public static final String FETCH_VIEWS_URL = DOMAIN + "/updateselfviews/";
    public static final String INCREMENT_VIEW_URL = DOMAIN + "/incrementviews/";
    public static final String FETCH_DATA_TYPES = DOMAIN + "/datatypes/";
    public static final String RECENT_URL = DOMAIN + "/recent/";
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    private MyHTTPHandler mMyHttpHandler;

    public RENServiceClientImpl(final MyHTTPHandler myHTTPHandler) {
        mMyHttpHandler = myHTTPHandler;
    }

    @Override
    public void updateSelfViews(final ArrayList<FileData> fileDataItems) {
        Log.i(TAG, "Updating self views");

        final String selfViews = getJSONSelfViews(fileDataItems);
        final Map<String, String> params = CommonUtility.getSecureParams();
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
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Update Error: ", error.getMessage());

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
    public void incrementViewsByOne(@NonNull final String fileId,
                                    @Nullable final IncrementViewsCallback incrementViewsCallback) {
        Log.i(TAG, "Incrementing the views by one for file ID=" + fileId);
        final Map<String, String> params = CommonUtility.getSecureParams();
        params.put("fileid", fileId);
        final CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST,
                INCREMENT_VIEW_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        Log.i(TAG, "Successfully updated views by one. file ID=" + fileId
                                + "; response=" + response);

                        if (incrementViewsCallback != null) {
                            incrementViewsCallback.onSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                final int statusCode = error.networkResponse.statusCode;
                Log.i(TAG, "Unable to update views by one. error=" + statusCode);

                if (incrementViewsCallback != null) {
                    incrementViewsCallback.onError(statusCode);
                }
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
                final int statusCode = error.networkResponse.statusCode;
                Log.e(TAG, "Unable to fetch data types. error=" + statusCode);
                fetchDataTypesCallback.onError(statusCode);
            }
        };

        final Map<String, String> params = CommonUtility.getSecureParams();
        final CustomRequest fetchRequest = new CustomRequest(Request.Method.POST,
                FETCH_DATA_TYPES, params, successResponseListener,
                errorResponseListener);

        mMyHttpHandler.addToRequestQueue(fetchRequest);
    }

    private void fetchFiles(final int offset, final String dataType, final String url,
                            final FetchFilesCallback fetchFilesCallback) {
        final Map<String, String> params = CommonUtility.getSecureParams();
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
                final int statusCode = error.networkResponse.statusCode;
                Log.i(TAG, "Unable to receive files response; error" + statusCode);
                fetchFilesCallback.onError(statusCode);
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
}
