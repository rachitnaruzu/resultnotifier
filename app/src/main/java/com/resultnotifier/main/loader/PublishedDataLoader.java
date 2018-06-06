package com.resultnotifier.main.loader;

import com.resultnotifier.main.db.DatabaseManager;
import com.resultnotifier.main.service.RENServiceClient;

public class PublishedDataLoader extends ServiceDataLoader {
    private final RENServiceClient mRenServiceClient;
    private final DatabaseManager mDatabaseManager;

    public PublishedDataLoader(final RENServiceClient renServiceClient,
                               final DatabaseManager databaseManager) {
        mRenServiceClient = renServiceClient;
        mDatabaseManager = databaseManager;
    }

    @Override
    public void fetchData(final Integer offset,
                          final String dataTypes,
                          final DataLoaderCallback dataLoaderCallback) {
        mRenServiceClient.fetchPublishedFiles(offset, dataTypes,
                getFetchFilesCallback(dataLoaderCallback, mDatabaseManager));
    }
}
