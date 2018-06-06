package com.resultnotifier.main.loader;

import com.resultnotifier.main.db.DatabaseManager;
import com.resultnotifier.main.FileData;

import java.util.ArrayList;

public class SavedDataLoader implements DataLoader {
    private final DatabaseManager mDatabaseManager;

    public SavedDataLoader(final DatabaseManager databaseManager) {
        mDatabaseManager = databaseManager;
    }

    @Override
    public void fetchData(final Integer offset,
                          final String dataTypes,
                          final DataLoaderCallback dataLoaderCallback) {
        final ArrayList<FileData> files =
                mDatabaseManager.getAllFiles(mDatabaseManager.getFilterSavedCheck());
        for(final FileData file : files){
            file.setIsCompleted(true);
        }

        dataLoaderCallback.onSuccess(files);
    }
}
