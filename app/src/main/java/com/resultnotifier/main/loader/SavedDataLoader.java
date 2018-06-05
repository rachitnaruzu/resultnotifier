package com.resultnotifier.main.loader;

import com.resultnotifier.main.DatabaseUtility;
import com.resultnotifier.main.FileData;

import java.util.ArrayList;

public class SavedDataLoader implements DataLoader {
    private final DatabaseUtility mDatabaseUtility;

    public SavedDataLoader(final DatabaseUtility databaseUtility) {
        mDatabaseUtility = databaseUtility;
    }

    @Override
    public void fetchData(final int offset,
                          final String dataTypes,
                          final DataLoaderCallback dataLoaderCallback) {
        final ArrayList<FileData> files =
                mDatabaseUtility.getAllFiles(mDatabaseUtility.getFilterSavedCheck());
        for(final FileData file : files){
            file.setIsCompleted(true);
        }

        dataLoaderCallback.onSuccess(files);
    }
}
