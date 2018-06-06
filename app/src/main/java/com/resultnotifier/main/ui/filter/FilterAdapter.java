package com.resultnotifier.main.ui.filter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.db.DatabaseManager;
import com.resultnotifier.main.R;

import java.util.ArrayList;

public class FilterAdapter extends BaseAdapter {
    private static final String TAG = "REN_FilterAdaptor";

    private final Activity mMainActivity;
    private final ArrayList<FilterItem> mFilterItems;
    private final DatabaseManager mDbUtil;
    private LayoutInflater mInflater;

    public FilterAdapter(final Activity mainActivity) {
        mMainActivity = mainActivity;
        mInflater = null;
        mDbUtil = AppState.getDatabaseManager(mMainActivity.getApplicationContext());
        mFilterItems = mDbUtil.getAllFilterItems();
        notifyDataSetChanged();
    }

    public void updateDatabase() {
        for (FilterItem filterItem : mFilterItems) {
            mDbUtil.updateDatatypeCheck(filterItem);
        }
    }

    @Override
    public int getCount() {
        return mFilterItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mFilterItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View vi = convertView;
        if (mInflater == null)
            mInflater = (LayoutInflater) mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (vi == null)
            vi = mInflater.inflate(R.layout.filter_list_item, null);
        TextView dataTypeView = (TextView) vi.findViewById(R.id.filter_data_type);
        CheckBox checkBox = (CheckBox) vi.findViewById(R.id.filter_checkBox);
        FilterItem filterItem = mFilterItems.get(position);
        final FilterItem current_filterItem = filterItem;
        checkBox.setOnClickListener(new View.OnClickListener() {
            FilterItem filterItem = current_filterItem;

            @Override
            public void onClick(View arg0) {
                filterItem.setIsChecked(!filterItem.isChecked());
            }
        });

        dataTypeView.setText(filterItem.getDataType());
        checkBox.setChecked(filterItem.isChecked());
        return vi;
    }
}
