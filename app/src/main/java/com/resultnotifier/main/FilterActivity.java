package com.resultnotifier.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ListView;


public class FilterActivity extends AppCompatActivity {

    private ListView mFilterList;
    private FilterAdapter mFilterAdapter;
    private CheckBox mFilterSavedCheckbox;
    private DatabaseUtility mDatabaseUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Toolbar filter_toolbar = (Toolbar) findViewById(R.id.filter_toolbar);
        setSupportActionBar(filter_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseUtility = AppState.getDatabaseUtility(getApplicationContext());
        mFilterSavedCheckbox = (CheckBox) findViewById(R.id.filter_saved_checkBox);
        mFilterSavedCheckbox.setChecked(mDatabaseUtility.getFilterSavedCheck());

        mFilterList = (ListView) findViewById(R.id.filter_list);
        mFilterAdapter = new FilterAdapter(this);
        mFilterList.setAdapter(mFilterAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.filter_action_save) {
            mFilterAdapter.updateDatabase();
            mDatabaseUtility.updateFilterSaved(mFilterSavedCheckbox.isChecked());
            Log.e("Save Pressed", "Save Pressed");
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            this.overridePendingTransition(R.anim.enter_from_left, R.anim.leave_to_right);
            return true;
        }

        if (id == android.R.id.home) {
            Log.e("Home", "Home");
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            this.overridePendingTransition(R.anim.enter_from_left, R.anim.leave_to_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
