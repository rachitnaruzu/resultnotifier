package com.resultnotifier.main;

import java.util.ArrayList;

public class TopmostFragment extends MainFragment {
    public TopmostFragment() {
        super.type = "topmost";
    }
    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0, TOPMOST_URL);
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
        handleNonSaved(TOPMOST_URL);
    }
}
