package com.resultnotifier.main;

import java.util.ArrayList;

/**
 * Created by User on 05-10-2015.
 */
public class PublishedFragment extends MainFragment {
    public PublishedFragment() {
        super.type = "published";
    }

    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0, PUBLISHED_URL);
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
        handleNonSaved(PUBLISHED_URL);
    }
}
