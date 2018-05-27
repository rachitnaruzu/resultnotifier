package com.resultnotifier.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class MainFragment extends Fragment {
    public static final String DOMAIN = CommonUtility.DOMAIN;
    public static final String KEY = CommonUtility.SECRET_KEY;
    public static final String PUBLISHED_URL = DOMAIN + "/published/";
    public static final String TOPMOST_URL = DOMAIN + "/topmost/";
    public static final String FETCH_VIEWS_URL = DOMAIN + "/updateselfviews/";
    public static final String INCREMENT_VIEW_URL = DOMAIN + "/incrementviews/";
    public static final String FETCH_DATA_TYPES = DOMAIN + "/datatypes/";
    public static final String RECENT_URL = DOMAIN + "/recent/";
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    private static final String TAG = "REN_MainFragment";
    private boolean mSelectFlag;
    private ListView mListView;
    private Activity mMainActivity;
    private MyAdaptor mListAdaptor;
    private DatabaseUtility mDbUtil;
    private String mDataType;
    private String mGlobalFileId;
    private Snackbar mSnackBar;
    private ImageView mNoNetworkIcon;
    private TextView mNoNetworkText;
    private boolean mRunningFlag;
    private TextView mNoContent;
    private View mFragmentView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SimpleMultiChoiceModeListener mMultiChoiceModeListener =
            new SimpleMultiChoiceModeListener();
    private int mVisibleCount;
    private String mCurrentURL;
    private ArrayList<FileData> mAllFileDataItems;
    private boolean mThatsIt;


    public MainFragment() {
    }

    public MyAdaptor getListAdaptor() {
        return mListAdaptor;
    }

    public DatabaseUtility getDatabaseUtility() {
        return mDbUtil;
    }

    public ListView getListView() {
        return mListView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public void setSelectedFlag(boolean isSelected) {
        mSelectFlag = isSelected;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String encodeStringData(String key, String value) {
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

    public static String getEncodedTimestamp() {
        Calendar currentTime = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String ts = dateFormat.format(currentTime.getTime());
        return Base64.encodeToString(ts.getBytes(), Base64.URL_SAFE);
    }

    public static Map<String, String> addSecureParams(Map<String, String> params) {
        String ts = getEncodedTimestamp();
        params.put("sec", ts);
        params.put("sig", encodeStringData(KEY, ts));
        return params;
    }

    void inflateArrayList(int offset, String url) {
        Map<String, String> params = new HashMap<>();
        params.put("offset", "" + offset);
        params.put("datatypes", mDataType);
        params = addSecureParams(params);
        CustomRequest fetchRequest = new CustomRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("files");
                            mThatsIt = jArray.length() < mVisibleCount;
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject oneObject = jArray.getJSONObject(i);
                                FileData filedata = new FileData();
                                filedata.mDateCreated = oneObject.getString("datecreated");
                                filedata.mDisplayName = oneObject.getString("displayname");
                                filedata.mUrl = oneObject.getString("url");
                                filedata.mDataType = oneObject.getString("datatype");
                                filedata.mViews = oneObject.getString("views");
                                filedata.mFileType = oneObject.getString("filetype");
                                filedata.mFileId = oneObject.getString("fileid");
                                filedata.mIsCompleted = mDbUtil.isFilePresent(filedata.mFileId);
                                if (filedata.mIsCompleted) {
                                    mDbUtil.updateViews(filedata);
                                }
                                mListAdaptor.add_items(filedata);
                            }
                            mRunningFlag = false;
                            showLoading(false);
                            mListAdaptor.notifyDataSetChanged();
                            showNoContent(mListAdaptor.getCount() == 0);
                            Log.i(TAG, response.toString());
                            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    //showNoNetwork(false);
                                    refreshFragment();
                                }
                            });
                        } catch (Exception e) {
                            VolleyLog.v(TAG, response.toString());
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                            Log.e(TAG, response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                //displayNoConnectionFragment();
                mListAdaptor.clear();
                mListAdaptor.notifyDataSetChanged();
                mSnackBar.setText("No Network");
                mSnackBar.show();
                showLoading(false);
                showNoNetwork(true);
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        showNoNetwork(false);
                        refreshFragment();
                    }
                });
            }
        });
        MyHTTPHandler.getInstance(mMainActivity.getApplicationContext()).addToRequestQueue(fetchRequest);
    }

    private String getJSONSelfViews(ArrayList<FileData> fileDataItems) {
        StringBuilder selfViewsB = new StringBuilder("[");
        boolean is_any_one_self_view = false;
        for (FileData fileData : fileDataItems) {
            //mListAdaptor.add_items(fileData);
            if (!fileData.selfViews.equals("0")) {
                is_any_one_self_view = true;
                selfViewsB.append("{\"mFileId\":\"" + fileData.mFileId + "\",\"selfviews\":\"" + fileData.selfViews + "\"}" + ",");
            }
        }
        selfViewsB.replace(selfViewsB.length() - 1, selfViewsB.length(), "]");
        String selfViews = "[]";
        if (is_any_one_self_view) selfViews = selfViewsB.toString();
        return selfViews;
        //return selfViewsB.toString();
    }

    private void updateSelfViews(ArrayList<FileData> fileDataItems) {
        String selfViews = getJSONSelfViews(fileDataItems);
        mAllFileDataItems = fileDataItems;
        Map<String, String> params = new HashMap<>();
        params.put("selfviews", selfViews);
        params = addSecureParams(params);
        if (!selfViews.equals("{}")) {
            CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST, FETCH_VIEWS_URL, params,
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
            MyHTTPHandler.getInstance(mMainActivity.getApplicationContext()).addToRequestQueue(updateSelfViewsRequest);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Date date = new Date();
        SimpleDateFormat sdf;
        super.onCreate(savedInstanceState);
        mMainActivity = getActivity();
        mDbUtil = DatabaseUtility.getInstance(mMainActivity.getApplicationContext());
        mDataType = mDbUtil.getCheckedDataTypes();
        mSelectFlag = false;
        mThatsIt = false;
        Log.i("Oncreate", "Oncreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("Oncreateview", "Oncreateview");
        View vi = inflater.inflate(R.layout.result, container, false);
        mFragmentView = vi;
        mSnackBar = Snackbar.make(vi.findViewById(R.id.myCoordinatorLayout), R.string.no_network_message, Snackbar.LENGTH_LONG);


        mListView = (ListView) vi.findViewById(R.id.listview);
        mListAdaptor = new MyAdaptor(mMainActivity);
        mListView.setAdapter(mListAdaptor);

        mNoContent = (TextView) vi.findViewById(R.id.no_content);
        mNoNetworkIcon = (ImageView) vi.findViewById(R.id.no_network_icon);
        mNoNetworkText = (TextView) vi.findViewById(R.id.no_network_text);

        mSwipeRefreshLayout = (SwipeRefreshLayout) vi.findViewById(R.id.swiperefresh);
        showLoading(false);
        showNoNetwork(false);
        showNoContent(false);
        setMultiChoice(mListView);
        MainActivity.setmSnackbar(mSnackBar);

        updateSelfViews(mDbUtil.getAllFiles(false));
        onCreateViewFinal();

        return vi;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMultiChoiceModeListener.exitActionMode();
    }

    public void refreshFragment() {
        mDataType = mDbUtil.getCheckedDataTypes();
        mSelectFlag = false;
        mThatsIt = false;
        mListAdaptor.clear();
        mListAdaptor.notifyDataSetChanged();
        refreshFragmentFinal();
    }

    public abstract void refreshFragmentFinal();

    public abstract void deleteSelectedItems();

    private void cancelSelect() {
        ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
        for (FileData fileData : mItems) {
            if (fileData.isSelected) {
                fileData.isSelected = false;
                //fileData.displaySelected = false;
            }
        }
        mMultiChoiceModeListener.exitActionMode();
        mListAdaptor.notifyDataSetChanged();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 500ms
                ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
                for (FileData fileData : mItems) {
                    fileData.displaySelected = false;
                }
            }
        }, 500);
        mSelectFlag = false;
    }

    public void incrementViewsByOne(String fileid) {
        mGlobalFileId = fileid;
        Map<String, String> params = new HashMap<>();
        params.put("mFileId", fileid);
        params = addSecureParams(params);
        CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST, INCREMENT_VIEW_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //DatabaseUtility.getInstance(mMainActivity.getApplicationContext()).incrementViewsByOne(mGlobalFileId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DatabaseUtility.getInstance(mMainActivity.getApplicationContext()).incrementSelfViewsByOne(mGlobalFileId);
                VolleyLog.e("Increment Error: ", error.getMessage());
            }
        });
        MyHTTPHandler.getInstance(mMainActivity.getApplicationContext()).addToRequestQueue(updateSelfViewsRequest);
    }

    public void openFile(FileData fileData) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/xmls");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileData.mFileId + "." + fileData.mFileType);
        if (!file.exists()) {

            Snackbar snackbar = Snackbar
                    .make(mFragmentView.findViewById(R.id.myCoordinatorLayout),
                            "File does not exists", Snackbar.LENGTH_INDEFINITE)
                    .setAction("REDOWNLOAD", new SnackBarFileDownloadClickListener(fileData));

            snackbar.show();
            return;
        }
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(fileData.mFileType);

        Uri apkURI = FileProvider.getUriForFile(
                mMainActivity, mMainActivity.getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkURI, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        grantAllUriPermissions(mMainActivity, intent, apkURI);
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            mSnackBar.setText("No app found to open the file");
            mSnackBar.show();
        }
    }

    private void grantAllUriPermissions(Context context, Intent intent, Uri uri) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public void setMultiChoice(ListView lv) {
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(mMultiChoiceModeListener);
    }

    public abstract void onCreateViewFinal();

    public void handleNonSaved(String url) {
        this.mCurrentURL = url;
        inflateArrayList(0, mCurrentURL);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileData mItem = (FileData) mListAdaptor.getItem(position);
                if (mItem.mIsCompleted) {
                    openFile(mItem);
                    incrementViewsByOne(mItem.mFileId);
                } else {
                    if (mItem.downloadjob == null || !mItem.getInProcess()) {
                        mItem.downloadjob = new DownloadJob(mMainActivity, mItem, mListAdaptor);
                        //mListAdaptor.notifyDataSetChanged();
                    }
                }
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!mThatsIt && firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    mVisibleCount = visibleItemCount;
                    if (!mRunningFlag) {
                        mRunningFlag = true;
                        inflateArrayList(totalItemCount, mCurrentURL);
                    }
                }
            }
        });
    }

    public void showLoading(boolean show) {
        if (show) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        } else {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    public void showNoNetwork(boolean show) {
        if (show) {
            mNoNetworkIcon.setVisibility(View.VISIBLE);
            mNoNetworkText.setVisibility(View.VISIBLE);
        } else {
            mNoNetworkIcon.setVisibility(View.INVISIBLE);
            mNoNetworkText.setVisibility(View.INVISIBLE);
        }
    }

    public void showNoContent(boolean show) {
        if (show) {
            mNoContent.setVisibility(View.VISIBLE);
        } else {
            mNoContent.setVisibility(View.INVISIBLE);
        }
    }

    public class SnackBarFileDownloadClickListener implements View.OnClickListener {
        FileData filedata;

        public SnackBarFileDownloadClickListener(FileData filedata) {
            this.filedata = filedata;
        }

        @Override
        public void onClick(View v) {
            filedata.downloadjob = new DownloadJob(mMainActivity, filedata, mListAdaptor);
        }
    }

    public class SimpleMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        ActionMode global_mode;
        //MenuItem global_item;
        TextView feed_update_count;
        ImageView menuItemDelete;
        int completed_count;
        int incompleted_count;

        public void exitActionMode() {
            if (global_mode != null) {
                global_mode.finish();
            }
            completed_count = incompleted_count = 0;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            FileData fileData = (FileData) mListAdaptor.getItem(position);
            if (fileData.mIsCompleted) {
                completed_count += checked ? 1 : -1;
            } else {
                incompleted_count += checked ? 1 : -1;
            }
            fileData.isSelected = checked;
            mListAdaptor.notifyDataSetChanged();
            feed_update_count.setText(String.valueOf(completed_count + incompleted_count));
            if (incompleted_count > 0) {
                menuItemDelete.setVisibility(View.INVISIBLE);
            } else {
                menuItemDelete.setVisibility(View.VISIBLE);
            }
        }

        private String getPresentsbleSelectedString() {
            ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
            String msg = "";
            for (FileData fileData : mItems) {
                if (fileData.isSelected) {
                    msg += fileData.mDisplayName + "\n" + fileData.mUrl + "\n\n";
                }
            }
            return msg;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            completed_count = incompleted_count = 0;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);

            LayoutInflater layoutInflater = (LayoutInflater) mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Animation translation = AnimationUtils.loadAnimation(mMainActivity, R.anim.enter_from_right);
            this.global_mode = mode;

            menuItemDelete = (ImageView) layoutInflater.inflate(R.layout.action_bar_item, null);
            menuItemDelete.setImageResource(R.drawable.ic_delete_white_18pt_3x);
            MenuItem deleteItem = menu.findItem(R.id.delete);
            deleteItem.setActionView(menuItemDelete);

            //this.global_item = item;
            menuItemDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteSelectedItems();
                    //global_mode.finish(); // Action picked, so close the CAB
                    exitActionMode();
                }
            });

            ImageView menuItemShare = (ImageView) layoutInflater.inflate(R.layout.action_bar_item, null);
            menuItemShare.setImageResource(R.drawable.ic_share_white_18pt_3x);
            MenuItem shareItem = menu.findItem(R.id.share);
            shareItem.setActionView(menuItemShare);
            //this.global_mode = mode;
            //this.global_item = item;
            menuItemShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getPresentsbleSelectedString());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "Share With"));
                    //global_mode.finish(); // Action picked, so close the CAB
                    exitActionMode();
                }
            });

            feed_update_count = (TextView) layoutInflater.inflate(R.layout.feed_update_count, null);
            feed_update_count.setText(String.valueOf(completed_count + incompleted_count));
            MenuItem selectCountItem = menu.findItem(R.id.selectCount);
            selectCountItem.setActionView(feed_update_count);

            menuItemDelete.startAnimation(translation);
            feed_update_count.startAnimation(translation);
            menuItemShare.startAnimation(translation);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.delete:
                    deleteSelectedItems();
                    global_mode = mode;
                    exitActionMode();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
            cancelSelect();
            exitActionMode();
        }
    }

}