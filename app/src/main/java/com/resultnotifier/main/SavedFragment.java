package com.resultnotifier.main;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class SavedFragment extends MainFragment {

    public SavedFragment() {
    }

    public void refreshFragmentFinal(){
        final DatabaseUtility dbUtil = getDatabaseUtility();
        final MyAdaptor listAdaptor = getListAdaptor();
        ArrayList<FileData> fileDataItems =  dbUtil.getAllFiles(dbUtil.getFilerSavedCheck());
        for(FileData fileData : fileDataItems){
            listAdaptor.add_items(fileData);
            fileData.mIsCompleted = true;
        }
        listAdaptor.notifyDataSetChanged();
        showNoContent(listAdaptor.getCount() == 0);
    }

    public void deleteSelectedItems(){
        final MyAdaptor listAdaptor = getListAdaptor();
        final DatabaseUtility dbUtil = getDatabaseUtility();
        ArrayList<FileData> mItems = new ArrayList<>(listAdaptor.getAdapterItems());
        listAdaptor.clear();// = new MyAdaptor(mainActivity);
        for(FileData fileData : mItems){
            if(fileData.isSelected){
                dbUtil.deleteFile(fileData);
            } else {
                listAdaptor.add_items(fileData);
            }
        }
        //lv.setAdapter(listAdaptor);
        listAdaptor.notifyDataSetChanged();
        setSelectedFlag(false);
        showNoContent(listAdaptor.getCount() == 0);
    }

    public void onCreateViewFinal(){
        final DatabaseUtility dbUtil = getDatabaseUtility();
        final MyAdaptor listAdaptor = getListAdaptor();
        final ListView listView = getListView();
        final SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        ArrayList<FileData> fileDataItems = dbUtil.getAllFiles(dbUtil.getFilerSavedCheck());
            for(FileData fileData : fileDataItems){
                listAdaptor.add_items(fileData);
                fileData.mIsCompleted = true;
            }
        listAdaptor.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileData mItem = (FileData) listAdaptor.getItem(position);
                openFile(mItem);
                incrementViewsByOne(mItem.mFileId);
            }
        });
        showNoContent(listAdaptor.getCount() == 0);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
