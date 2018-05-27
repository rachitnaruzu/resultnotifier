package com.resultnotifier.main;

import java.util.ArrayList;


public class RecentFragment extends MainFragment {
    public RecentFragment() {
    }

    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0, RECENT_URL);
    }
    public void deleteSelectedItems(){
        final MyAdaptor listAdapter = getListAdaptor();
        final DatabaseUtility dbUtil = getDatabaseUtility();
        ArrayList<FileData> mItems = listAdapter.getAdapterItems();
        for(FileData fileData : mItems){
            if(fileData.isSelected){
                dbUtil.deleteFile(fileData);
                fileData.mIsCompleted = false;
            }
        }
        listAdapter.notifyDataSetChanged();
        setSelectedFlag(false);
    }
    public void onCreateViewFinal(){
        showLoading(true);
        handleNonSaved(RECENT_URL);
    }
}
