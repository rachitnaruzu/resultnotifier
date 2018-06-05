package com.resultnotifier.main.loader;

import com.resultnotifier.main.DatabaseUtility;
import com.resultnotifier.main.service.RENServiceClient;

public class PublishedDataLoader extends ServiceDataLoader {
    private final RENServiceClient mRenServiceClient;
    private final DatabaseUtility mDatabaseUtility;

    public PublishedDataLoader(final RENServiceClient renServiceClient,
                               final DatabaseUtility databaseUtility) {
        mRenServiceClient = renServiceClient;
        mDatabaseUtility = databaseUtility;
    }

    @Override
    public void fetchData(final int offset,
                          final String dataTypes,
                          final DataLoaderCallback dataLoaderCallback) {
        mRenServiceClient.fetchPublishedFiles(offset, dataTypes,
                getFetchFilesCallback(dataLoaderCallback, mDatabaseUtility));
    }
}
