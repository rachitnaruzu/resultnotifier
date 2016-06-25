package com.resultnotifier.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public abstract class MainFragment extends Fragment {
    ListView lv;
    Activity mainActivity;
    String type;
    MyAdaptor mListAdaptor;
    private boolean running_flag;
    DatabaseUtility dbUtil;
    String datatypes;
    private String global_fileid;
    private ActionMode mActionMode;
    Snackbar mSnackbar;
    //private ProgressBar loadingSpinner;
    private ImageView no_network_icon;
    private TextView no_network_text;
    //private Button retry_button;
    private TextView no_content;
    private View fragmentView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private SimpleMultiChoiceModeListener multiChoiceModeListener = new SimpleMultiChoiceModeListener();


    public MainFragment() {}

    public final static String DOMAIN = CommonUtility.DOMAIN;
    public final static String KEY = CommonUtility.SECRET_KEY;
    public final static String PUBLISHED_URL = DOMAIN + "/published/";
    public final static String TOPMOST_URL = DOMAIN + "/topmost/";
    public final static String FETCH_VIEWS_URL = DOMAIN + "/updateselfviews/";
    public final static String INCREMENT_VIEW_URL = DOMAIN + "/incrementviews/";
    public final static String FETCH_DATA_TYPES = DOMAIN + "/datatypes/";
    public final static String RECENT_URL = DOMAIN + "/recent/";
    private int visibleCount;
    private String currentURL;
    private ArrayList<FileData> all_fileDataItems;
    boolean selectFlag;
    private boolean thatsit;


    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
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

    public static String getEncodedTimestamp(){
        Calendar currentTime = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String ts = dateFormat.format(currentTime.getTime());
        return Base64.encodeToString(ts.getBytes(), Base64.URL_SAFE);
    }

    public static Map<String, String> addSecureParams(Map<String, String> params){
        String ts = getEncodedTimestamp();
        params.put("sec", ts);
        params.put("sig", encodeStringData(KEY, ts));
        return params;
    }

    void inflateArrayList(int offset, String url) {
        Map<String, String> params = new HashMap<>();
        params.put("offset", "" + offset);
        params.put("datatypes", datatypes);
        params = addSecureParams(params);
        CustomRequest fetchRequest = new CustomRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("files");
                            thatsit = jArray.length() < visibleCount;
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject oneObject = jArray.getJSONObject(i);
                                FileData filedata = new FileData();
                                filedata.datecreated = oneObject.getString("datecreated");
                                filedata.displayname = oneObject.getString("displayname");
                                filedata.url = oneObject.getString("url");
                                filedata.datatype = oneObject.getString("datatype");
                                filedata.views = oneObject.getString("views");
                                filedata.filetype = oneObject.getString("filetype");
                                filedata.fileid = oneObject.getString("fileid");
                                filedata.iscompleted = dbUtil.isFilePresent(filedata.fileid);
                                if(filedata.iscompleted){
                                    dbUtil.updateViews(filedata);
                                }
                                mListAdaptor.add_items(filedata);
                            }
                            running_flag = false;
                            showLoading(false);
                            mListAdaptor.notifyDataSetChanged();
                            showNoContent(mListAdaptor.getCount() == 0);
                            Log.i(type + "-data: ", response.toString());
                            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    //showNoNetwork(false);
                                    refreshFragment();
                                }
                            });
                        } catch (Exception e) {
                            VolleyLog.v(type + "-data: ", response.toString());
                            Log.e(type + "-data: ", e.toString());
                            e.printStackTrace();
                            Log.e(type + "-data: ", response.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("InflateArray Error: ", error.getMessage());
                //displayNoConnectionFragment();
                mListAdaptor.clear();
                mListAdaptor.notifyDataSetChanged();
                mSnackbar.setText("No Network");
                mSnackbar.show();
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
        MyHTTPHandler.getInstance(mainActivity.getApplicationContext()).addToRequestQueue(fetchRequest);
    }

    private String getJSONSelfViews(ArrayList<FileData> fileDataItems) {
        StringBuilder selfViewsB = new StringBuilder("[");
        boolean is_any_one_self_view = false;
        for (FileData fileData : fileDataItems) {
            //mListAdaptor.add_items(fileData);
            if (!fileData.selfViews.equals("0")) {
                is_any_one_self_view = true;
                selfViewsB.append("{\"fileid\":\"" + fileData.fileid + "\",\"selfviews\":\"" + fileData.selfViews + "\"}" + ",");
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
        all_fileDataItems = fileDataItems;
        Map<String, String> params = new HashMap<>();
        params.put("selfviews", selfViews);
        params = addSecureParams(params);
        if(!selfViews.equals("{}")) {
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
            MyHTTPHandler.getInstance(mainActivity.getApplicationContext()).addToRequestQueue(updateSelfViewsRequest);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Date date = new Date();
        SimpleDateFormat sdf;
        super.onCreate(savedInstanceState);
        mainActivity = getActivity();
        dbUtil = DatabaseUtility.getInstance(mainActivity.getApplicationContext());
        datatypes = dbUtil.getCheckedDataTypes();
        selectFlag = false;
        thatsit = false;
        Log.i("Oncreate", "Oncreate");
    }


    public void refreshFragment(){
        datatypes = dbUtil.getCheckedDataTypes();
        selectFlag = false;
        thatsit = false;
        mListAdaptor.clear();
        mListAdaptor.notifyDataSetChanged();
        refreshFragmentFinal();
    }

    public abstract void refreshFragmentFinal();


    public abstract void deleteSelectedItems();


    private void cancelSelect(){
        ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
        for(FileData fileData : mItems){
            if(fileData.isSelected) {
                fileData.isSelected = false;
                //fileData.displaySelected = false;
            }
        }
        multiChoiceModeListener.exitActionMode();
        mListAdaptor.notifyDataSetChanged();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                // Do something after 500ms
                ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
                for(FileData fileData : mItems){
                    fileData.displaySelected = false;
                }
            }
        }, 500);
        selectFlag = false;
    }



    public void incrementViewsByOne(String fileid){
        global_fileid = fileid;
        Map<String, String> params = new HashMap<>();
        params.put("fileid", fileid);
        params = addSecureParams(params);
        CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST, INCREMENT_VIEW_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //DatabaseUtility.getInstance(mainActivity.getApplicationContext()).incrementViewsByOne(global_fileid);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DatabaseUtility.getInstance(mainActivity.getApplicationContext()).incrementSelfViewsByOne(global_fileid);
                VolleyLog.e("Increment Error: ", error.getMessage());
            }
        });
        MyHTTPHandler.getInstance(mainActivity.getApplicationContext()).addToRequestQueue(updateSelfViewsRequest);
    }

    public class SnackBarFileDownloadClickListener implements View.OnClickListener {
        FileData filedata;
        public SnackBarFileDownloadClickListener(FileData filedata){
            this.filedata = filedata;
        }
        @Override
        public void onClick(View v) {
            filedata.downloadjob = new DownloadJob(mainActivity, filedata, mListAdaptor);
        }
    }

    public void openFile(FileData fileData){
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/xmls");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileData.fileid + "." + fileData.filetype);
        if(!file.exists()){

            Snackbar snackbar = Snackbar
                    .make(fragmentView.findViewById(R.id.myCoordinatorLayout),
                            "File does not exists",Snackbar.LENGTH_INDEFINITE)
                    .setAction("REDOWNLOAD", new SnackBarFileDownloadClickListener(fileData));

            snackbar.show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(fileData.filetype);
        intent.setDataAndType(Uri.fromFile(file), mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try{
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex){
            mSnackbar.setText("No app found to open the file");
            mSnackbar.show();
        }
    }

    public class SimpleMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        ActionMode global_mode;
        //MenuItem global_item;
        TextView feed_update_count;
        ImageView menuItemDelete;
        int completed_count;
        int incompleted_count;

        public void exitActionMode(){
            if(global_mode != null){
                global_mode.finish();
            }
            completed_count = incompleted_count = 0;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            //View view = mode.getCustomView();
            FileData fileData = (FileData) mListAdaptor.getItem(position);
            if (fileData.iscompleted) {
                completed_count += checked ? 1 : -1;
            } else {
                incompleted_count += checked ? 1 : -1;
            }
            fileData.isSelected = checked;
            mListAdaptor.notifyDataSetChanged();
            //total_count += checked ? 1 : -1;
            feed_update_count.setText(String.valueOf(completed_count + incompleted_count));
            if(incompleted_count > 0){
                menuItemDelete.setVisibility(View.INVISIBLE);
            } else {
                menuItemDelete.setVisibility(View.VISIBLE);
            }
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

        private String getPresentsbleSelectedString(){
            ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
            String msg = "";
            for(FileData fileData : mItems){
                if(fileData.isSelected) {
                    msg += fileData.displayname + "\n" + fileData.url + "\n\n";
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

            LayoutInflater layoutInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Animation translation = AnimationUtils.loadAnimation(mainActivity, R.anim.enter_from_right);
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
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
            cancelSelect();
            exitActionMode();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    }

    public void setMultiChoice(ListView lv){
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(multiChoiceModeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        multiChoiceModeListener.exitActionMode();
    }

    public abstract void onCreateViewFinal();

    public void handleNonSaved(String url){
        this.currentURL = url;
        inflateArrayList(0, currentURL);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileData mItem = (FileData) mListAdaptor.getItem(position);
                if (mItem.iscompleted) {
                    openFile(mItem);
                    incrementViewsByOne(mItem.fileid);
                } else {
                    if (mItem.downloadjob == null || !mItem.getInProcess()) {
                        mItem.downloadjob = new DownloadJob(mainActivity, mItem, mListAdaptor);
                        //mListAdaptor.notifyDataSetChanged();
                    }
                }
            }
        });

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!thatsit && firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    visibleCount = visibleItemCount;
                    if (!running_flag) {
                        running_flag = true;
                        inflateArrayList(totalItemCount, currentURL);
                    }
                }
            }
        });
    }

    public void showLoading(boolean show){
        if (show) {
            //lv.setVisibility(View.INVISIBLE);
            //loadingSpinner.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        } else {
           // loadingSpinner.setVisibility(View.INVISIBLE);
            //lv.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    public void showNoNetwork(boolean show){
        if(show){
            no_network_icon.setVisibility(View.VISIBLE);
            no_network_text.setVisibility(View.VISIBLE);
            //retry_button.setVisibility(View.VISIBLE);
        } else {
            no_network_icon.setVisibility(View.INVISIBLE);
            no_network_text.setVisibility(View.INVISIBLE);
            //retry_button.setVisibility(View.INVISIBLE);
        }
    }

    public void showNoContent(boolean show){
        if(show){
            no_content.setVisibility(View.VISIBLE);
        } else {
            no_content.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("Oncreateview", "Oncreateview");
        View vi = inflater.inflate(R.layout.result, container, false);
        fragmentView = vi;
        mSnackbar = Snackbar.make(vi.findViewById(R.id.myCoordinatorLayout), R.string.no_network_message, Snackbar.LENGTH_LONG);


        lv = (ListView) vi.findViewById(R.id.listview);
        mListAdaptor = new MyAdaptor(mainActivity);
        lv.setAdapter(mListAdaptor);

        no_content = (TextView) vi.findViewById(R.id.no_content);
        no_network_icon = (ImageView) vi.findViewById(R.id.no_network_icon);
        no_network_text = (TextView) vi.findViewById(R.id.no_network_text);

        mSwipeRefreshLayout = (SwipeRefreshLayout) vi.findViewById(R.id.swiperefresh);
        showLoading(false);
        showNoNetwork(false);
        showNoContent(false);
        setMultiChoice(lv);
        MainActivity.setmSnackbar(mSnackbar);

        updateSelfViews(dbUtil.getAllFiles(false));
        onCreateViewFinal();

        return vi;
    }

}