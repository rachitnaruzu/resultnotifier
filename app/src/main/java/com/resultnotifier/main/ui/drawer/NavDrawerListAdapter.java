package com.resultnotifier.main.ui.drawer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.resultnotifier.main.R;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {
    private final Context mContext;
    private final ArrayList<NavDrawerItem> mNavDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        mContext = context;
        mNavDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return mNavDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mNavDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = convertView.findViewById(R.id.icon);
        TextView txtTitle = convertView.findViewById(R.id.title);

        imgIcon.setImageResource(mNavDrawerItems.get(position).getIcon());
        txtTitle.setText(mNavDrawerItems.get(position).getTitle());

        return convertView;
    }
}