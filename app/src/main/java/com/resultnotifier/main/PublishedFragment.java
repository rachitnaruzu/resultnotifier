package com.resultnotifier.main;

import com.resultnotifier.main.service.RENServiceClient;

import java.util.ArrayList;

public class PublishedFragment extends MainFragment {
    public PublishedFragment() {
    }

    @Override
    public void fetchFiles(final RENServiceClient renServiceClient,
                           final int offset, final String dataType,
                           final RENServiceClient.FetchFilesCallback filesResponse) {
        renServiceClient.fetchPublishedFiles(offset, dataType, filesResponse);
    }

    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0);
    }

    public void deleteSelectedItems(){
        final MyAdaptor listAdapter = getListAdaptor();
        final DatabaseUtility dbUtil = getDatabaseUtility();
        ArrayList<FileData> mItems = listAdapter.getAdapterItems();
        for(FileData fileData : mItems){
            if(fileData.isSelected()){
                dbUtil.deleteFile(fileData);
                fileData.setIsCompleted(false);
            }
        }
        listAdapter.notifyDataSetChanged();
        setSelectedFlag(false);
    }

    public void onCreateViewFinal(){
        showLoading(true);
        handleNonSaved();
    }
}
