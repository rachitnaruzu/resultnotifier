package com.resultnotifier.main;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class FilterAdapter extends BaseAdapter {

    private final Activity mMainActivity;
    private final ArrayList<FilterItem> mFilterItems;
    private final DatabaseUtility mDbUtil;
    private LayoutInflater mInflater;

    public FilterAdapter(final Activity mainActivity) {
        mMainActivity = mainActivity;
        mInflater = null;
        mDbUtil = DatabaseUtility.getInstance(mMainActivity.getApplicationContext());
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
                //final boolean isChecked = mCheckBox.isChecked();
                filterItem.is_checked = !filterItem.is_checked;
                for (FilterItem filterItem : mFilterItems) {
                    Log.e(filterItem.datatype, filterItem.is_checked + "");
                }
            }
        });
        dataTypeView.setText(filterItem.datatype);
        checkBox.setChecked(filterItem.is_checked);
        return vi;
    }
}
