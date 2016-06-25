package com.resultnotifier.main;


import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private Toolbar mToolbar;
    private static int[] colors;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";


    MainFragment current_Fragment;

    private static Snackbar mSnackbar;

    public static void setmSnackbar(Snackbar mSnackbar){
        MainActivity.mSnackbar = mSnackbar;
    }

    public static Snackbar getmSnackbar(){
        return MainActivity.mSnackbar;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }

    public static int getRandomColor(){
        return colors[(int)(Math.random() * colors.length)];
    }

    private void requestDataTypes(){
        Map<String, String> params = new HashMap<>();
        params = MainFragment.addSecureParams(params);
        CustomRequest fetchRequest = new CustomRequest(Request.Method.POST, MainFragment.FETCH_DATA_TYPES, params,
                new Response.Listener<JSONObject>() {
                    DatabaseUtility dbUtil = DatabaseUtility.getInstance(getApplicationContext());
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("datatypes");
                            for (int i = 0; i < jArray.length(); i++) {
                                FilterItem filterItem = new FilterItem();
                                filterItem.datatype = jArray.getString(i);
                                filterItem.is_checked = true;
                                if (!dbUtil.isDataTypeAvailable(filterItem.datatype)) {
                                    dbUtil.addDataType(filterItem);
                                }
                            }
                            Log.i("Filter Success", response.toString());
                        } catch (Exception e) {
                            VolleyLog.v("Filter onResponse Error: ", response.toString());
                            Log.e("onResponse Error: ", e.toString());
                            e.printStackTrace();
                            Log.e("onResponse Error: ", response.toString());

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("onErrorResponse Error: ", error.getMessage());

            }
        });
        MyHTTPHandler.getInstance(getApplicationContext()).addToRequestQueue(fetchRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mmain);

        requestDataTypes();

        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        mToolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mToolbar);
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        colors = getResources().getIntArray(R.array.dataType_icon_bg_colors);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        navDrawerItems = new ArrayList<>();
        for(int i = 0; i < navMenuTitles.length; i++){
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
        }

        navMenuIcons.recycle();
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.app_name,
                R.string.app_name
        );
        //getSupportActionBar().setTitle(navMenuTitles[0]);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (savedInstanceState == null) {
            displayView(0);
        }
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //displayView(0);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);


    }



    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_filter:
                Intent intent = new Intent(this, FilterActivity.class);
                startActivityForResult(intent, 0);
                this.overridePendingTransition(R.anim.enter_from_right,
                        R.anim.leave_to_left);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(current_Fragment != null){
                Log.e("Main_Activity","back to main activity");
                current_Fragment.refreshFragment();
            }
        }
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        //Fragment fragment = null;
        current_Fragment = null;
        switch (position) {
            case 0: current_Fragment = new PublishedFragment(); break;
            case 1: current_Fragment = new TopmostFragment(); break;
            case 2: current_Fragment = new RecentFragment(); break;
            case 3: current_Fragment = new SavedFragment(); break;
            default: break;
        }
        if (current_Fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, current_Fragment).commit();
            mDrawerList.setItemChecked(position, true);
            getSupportActionBar().setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        //setTitle(mTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonUtility.TotalNotificationCount = 0;
        CommonUtility.dataTypeMap.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}