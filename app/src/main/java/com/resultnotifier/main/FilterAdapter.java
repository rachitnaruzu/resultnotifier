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

/**
 * Created by rachitnaruzu on 03-06-2016.
 */
public class FilterAdapter extends BaseAdapter {

    Activity mainActivity;
    ArrayList<FilterItem> filterItems;
    LayoutInflater inflater;
    DatabaseUtility dbUtil;
    private CheckBox checkBox;
    private FilterItem current_filterItem;

    public FilterAdapter(Activity mainActivity){
        this.mainActivity = mainActivity;
        inflater = null;
        this.filterItems = new ArrayList<>();
        dbUtil = DatabaseUtility.getInstance(mainActivity.getApplicationContext());
        filterItems = dbUtil.getAllFilterItems();
        notifyDataSetChanged();
    }

    public void updateDatabase(){
        for(FilterItem filterItem : filterItems){
            dbUtil.updateDatatypeCheck(filterItem);
        }
    }

    @Override
    public int getCount() {
        return filterItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filterItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (inflater == null)
            inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (vi == null)
            vi = inflater.inflate(R.layout.filter_list_item, null);
        TextView dataTypeView = (TextView) vi.findViewById(R.id.filter_data_type);
        checkBox = (CheckBox) vi.findViewById(R.id.filter_checkBox);
        FilterItem filterItem = filterItems.get(position);
        current_filterItem = filterItem;
        checkBox.setOnClickListener(new View.OnClickListener() {
            FilterItem filterItem = current_filterItem;
            @Override
            public void onClick(View arg0) {
                //final boolean isChecked = checkBox.isChecked();
                filterItem.is_checked = !filterItem.is_checked;
                for(FilterItem filterItem : filterItems){
                    Log.e(filterItem.datatype, filterItem.is_checked + "");
                }
            }
        });
        dataTypeView.setText(filterItem.datatype);
        checkBox.setChecked(filterItem.is_checked);
        return vi;
    }
}
