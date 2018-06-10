package com.resultnotifier.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import com.resultnotifier.main.service.RENServiceClient;
import com.resultnotifier.main.service.RENServiceClientImpl;

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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "REN_MainActivity";
    private static int[] mColors;
    private static Snackbar mSnackbar;
    private MainFragment mCurrent_Fragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mNavMenuTitles;
    private NavDrawerListAdapter mAdapter;
    private Toolbar mToolbar;
    private DatabaseUtility mDatabaseUtility;
    private RENServiceClient mRenServiceClient;

    public static Snackbar getmSnackbar() {
        return MainActivity.mSnackbar;
    }

    public static void setmSnackbar(Snackbar mSnackbar) {
        MainActivity.mSnackbar = mSnackbar;
    }

    public static int getRandomColor() {
        return mColors[(int) (Math.random() * mColors.length)];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Creating MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mmain);

        mRenServiceClient = AppState.getRenServiceClient(getApplicationContext());

        fetchDataTypes();

        mToolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mToolbar);

        final Resources resources = getResources();

        mColors = resources.getIntArray(R.array.dataType_icon_bg_colors);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        mNavMenuTitles = resources.getStringArray(R.array.nav_drawer_items);
        final TypedArray navMenuIcons = resources.obtainTypedArray(R.array.nav_drawer_icons);
        final ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<>(mNavMenuTitles.length);
        for (int i = 0; i < mNavMenuTitles.length; i++) {
            navDrawerItems.add(new NavDrawerItem(mNavMenuTitles[i],
                    navMenuIcons.getResourceId(i, -1)));
        }
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        mAdapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar,
                R.string.app_name,
                R.string.app_name
        );

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        if (savedInstanceState == null) {
            displayView(0);
        }

        mDatabaseUtility = DatabaseUtility.getInstance(getApplicationContext());

        getSupportActionBar().setHomeButtonEnabled(true);
        Log.i(TAG, "MainActivity created");
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            if (mCurrent_Fragment != null) {
                Log.e(TAG, "back to main activity");
                mCurrent_Fragment.refreshFragment();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    private void fetchDataTypes() {
        Log.i(TAG, "Requesting data types");
        mRenServiceClient.fetchDataTypes(new RENServiceClient.FetchDataTypesCallback() {
            @Override
            public void onSuccess(final List<String> dataTypes) {
                saveDataTypes(dataTypes);
            }

            @Override
            public void onError(final int error) {
                Log.i(TAG, "Unable to fetch data types. error=" + error);
            }
        });
    }

    public RENServiceClient getRENServiceClient() {
        return mRenServiceClient;
    }

    private void saveDataTypes(final List<String> dataTypes) {
        for (final String dataType : dataTypes) {
            FilterItem filterItem = new FilterItem();
            filterItem.datatype = dataType;
            filterItem.is_checked = true;
            if (!mDatabaseUtility.isDataTypeAvailable(filterItem.datatype)) {
                mDatabaseUtility.addDataType(filterItem);
            }
        }
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        mCurrent_Fragment = null;
        switch (position) {
            case 0:
                mCurrent_Fragment = new PublishedFragment();
                break;
            case 1:
                mCurrent_Fragment = new TopmostFragment();
                break;
            case 2:
                mCurrent_Fragment = new RecentFragment();
                break;
            case 3:
                mCurrent_Fragment = new SavedFragment();
                break;
            default:
                break;
        }

        Log.i(TAG, "Displaying Fragment. position=" + position);
        if (mCurrent_Fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, mCurrent_Fragment).commit();
            mDrawerList.setItemChecked(position, true);
            getSupportActionBar().setTitle(mNavMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            Log.e(TAG, "Error in creating fragment. position=" + position);
        }
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }
}