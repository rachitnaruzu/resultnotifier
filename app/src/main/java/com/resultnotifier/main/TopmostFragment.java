package com.resultnotifier.main;

import java.util.ArrayList;

public class TopmostFragment extends MainFragment {
    public TopmostFragment() {
    }

    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0, TOPMOST_URL);
    }
    public void deleteSelectedItems(){
        final MyAdaptor listAdaptor = getListAdaptor();
        final DatabaseUtility dbUtil = getDatabaseUtility();
        ArrayList<FileData> mItems = listAdaptor.getAdapterItems();
        for(FileData fileData : mItems){
            if(fileData.isSelected){
                dbUtil.deleteFile(fileData);
                fileData.mIsCompleted = false;
            }
        }
        listAdaptor.notifyDataSetChanged();
        setSelectedFlag(false);
    }
    public void onCreateViewFinal(){
        showLoading(true);
        handleNonSaved(TOPMOST_URL);
    }
}
