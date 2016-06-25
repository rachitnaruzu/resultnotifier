package com.resultnotifier.main;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;


public class SavedFragment extends MainFragment {
    public SavedFragment() {
        super.type = "saved";
    }
    public void refreshFragmentFinal(){
        ArrayList<FileData> fileDataItems =  dbUtil.getAllFiles(dbUtil.getFilerSavedCheck());
        for(FileData fileData : fileDataItems){
            mListAdaptor.add_items(fileData);
            fileData.iscompleted = true;
        }
        mListAdaptor.notifyDataSetChanged();
        showNoContent(mListAdaptor.getCount() == 0);
    }
    public void deleteSelectedItems(){
        ArrayList<FileData> mItems = new ArrayList<>(mListAdaptor.getAdapterItems());
        mListAdaptor.clear();// = new MyAdaptor(mainActivity);
        for(FileData fileData : mItems){
            if(fileData.isSelected){
                dbUtil.deleteFile(fileData);
            } else {
                mListAdaptor.add_items(fileData);
            }
        }
        //lv.setAdapter(mListAdaptor);
        mListAdaptor.notifyDataSetChanged();
        selectFlag = false;
        showNoContent(mListAdaptor.getCount() == 0);
    }
    public void onCreateViewFinal(){
        ArrayList<FileData> fileDataItems = dbUtil.getAllFiles(dbUtil.getFilerSavedCheck());
            for(FileData fileData : fileDataItems){
                mListAdaptor.add_items(fileData);
                fileData.iscompleted = true;
            }
        mListAdaptor.notifyDataSetChanged();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileData mItem = (FileData) mListAdaptor.getItem(position);
                openFile(mItem);
                incrementViewsByOne(mItem.fileid);
            }
        });
        showNoContent(mListAdaptor.getCount() == 0);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
