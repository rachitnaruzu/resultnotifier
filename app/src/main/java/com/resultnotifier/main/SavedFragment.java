package com.resultnotifier.main;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.resultnotifier.main.service.RENServiceClient;

import java.util.ArrayList;


public class SavedFragment extends MainFragment {

    public SavedFragment() {
    }

    @Override
    public void fetchFiles(
            final RENServiceClient renServiceClient,
            final int offset,
            final String dataType,
            final RENServiceClient.FetchFilesCallback filesResponse) {
        //no-op
    }

    public void refreshFragmentFinal(){
        final DatabaseUtility dbUtil = getDatabaseUtility();
        final MyAdaptor listAdaptor = getListAdaptor();
        ArrayList<FileData> fileDataItems =  dbUtil.getAllFiles(dbUtil.getFilerSavedCheck());
        for(FileData fileData : fileDataItems){
            fileData.setIsCompleted(true);
            listAdaptor.add_items(fileData);
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
            if(fileData.isSelected()){
                dbUtil.deleteFile(fileData);
            } else {
                listAdaptor.add_items(fileData);
            }
        }

        listAdaptor.notifyDataSetChanged();
        setSelectedFlag(false);
        showNoContent(listAdaptor.getCount() == 0);
    }

    public void onCreateViewFinal(){
        final DatabaseUtility dbUtil = getDatabaseUtility();
        final MyAdaptor listAdaptor = getListAdaptor();
        final ListView listView = getListView();
        ArrayList<FileData> fileDataItems = dbUtil.getAllFiles(dbUtil.getFilerSavedCheck());
        for(FileData fileData : fileDataItems){
            listAdaptor.add_items(fileData);
            fileData.setIsCompleted(true);
        }

        showLoading(false);
        listAdaptor.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileData mItem = (FileData) listAdaptor.getItem(position);
                openFile(mItem);
                incrementViewsByOne(mItem.getFileId());
            }
        });
        showNoContent(listAdaptor.isEmpty());
    }
}
