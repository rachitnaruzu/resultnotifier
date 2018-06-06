package com.resultnotifier.main.loader;

import com.resultnotifier.main.DatabaseUtility;
import com.resultnotifier.main.service.RENServiceClient;

public class RecentDataLoader extends ServiceDataLoader {
    private final RENServiceClient mRenServiceClient;
    private final DatabaseUtility mDatabaseUtility;

    public RecentDataLoader(final RENServiceClient renServiceClient,
                            final DatabaseUtility databaseUtility) {
        mRenServiceClient = renServiceClient;
        mDatabaseUtility = databaseUtility;
    }

    @Override
    public void fetchData(final Integer offset,
                          final String dataTypes,
                          final DataLoaderCallback dataLoaderCallback) {
        mRenServiceClient.fetchRecentFiles(offset, dataTypes,
                getFetchFilesCallback(dataLoaderCallback, mDatabaseUtility));
    }
}
