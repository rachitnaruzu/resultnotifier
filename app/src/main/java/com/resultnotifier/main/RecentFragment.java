package com.resultnotifier.main;

import java.util.ArrayList;


public class RecentFragment extends MainFragment {
    public RecentFragment() {
        super.type = "recent";
    }
    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0, RECENT_URL);
    }
    public void deleteSelectedItems(){
        ArrayList<FileData> mItems = mListAdaptor.getAdapterItems();
        for(FileData fileData : mItems){
            if(fileData.isSelected){
                dbUtil.deleteFile(fileData);
                fileData.iscompleted = false;
            }
        }
        mListAdaptor.notifyDataSetChanged();
        selectFlag = false;
    }
    public void onCreateViewFinal(){
        showLoading(true);
        handleNonSaved(RECENT_URL);
    }
}
