package com.resultnotifier.main;

import com.resultnotifier.main.service.RENServiceClient;

import java.util.ArrayList;

public class TopmostFragment extends MainFragment {
    public TopmostFragment() {
    }

    @Override
    public void fetchFiles(final RENServiceClient renServiceClient,
                           final int offset, final String dataType,
                           final RENServiceClient.FetchFilesCallback filesResponse) {
        renServiceClient.fetchTopMostFiles(offset, dataType, filesResponse);
    }

    public void refreshFragmentFinal(){
        showLoading(true);
        inflateArrayList(0);
    }

    public void deleteSelectedItems(){
        final MyAdaptor listAdaptor = getListAdaptor();
        final DatabaseUtility dbUtil = getDatabaseUtility();
        ArrayList<FileData> mItems = listAdaptor.getAdapterItems();
        for(FileData fileData : mItems){
            if(fileData.isSelected()){
                dbUtil.deleteFile(fileData);
                fileData.setIsCompleted(false);
            }
        }
        listAdaptor.notifyDataSetChanged();
        setSelectedFlag(false);
    }

    public void onCreateViewFinal(){
        showLoading(true);
        handleNonSaved();
    }
}
