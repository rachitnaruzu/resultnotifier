package com.resultnotifier.main;

import android.content.Context;
import android.os.Handler;

import com.resultnotifier.main.downloader.FileDownloader;
import com.resultnotifier.main.downloader.FileDownloaderImpl;
import com.resultnotifier.main.loader.DataLoader;
import com.resultnotifier.main.loader.PublishedDataLoader;
import com.resultnotifier.main.loader.RecentDataLoader;
import com.resultnotifier.main.loader.SavedDataLoader;
import com.resultnotifier.main.loader.TopMostDataLoader;
import com.resultnotifier.main.service.RENServiceClient;
import com.resultnotifier.main.service.RENServiceClientImpl;

public class AppState {
    private static RENServiceClient REN_SERVICE_CLIENT;
    private static FileDownloader FILE_DOWNLOADER;
    private static DataLoader PUBLISHED_DATA_LOADER;
    private static DataLoader RECENT_DATA_LOADER;
    private static DataLoader SAVED_DATA_LOADER;
    private static DataLoader TOP_MOST_DATA_LOADER;
    private static DatabaseUtility DATABASE_UTILITY;

    public static RENServiceClient getRenServiceClient(final Context context) {
        if(REN_SERVICE_CLIENT == null) {
            final MyHTTPHandler myHTTPHandler =  new MyHTTPHandler(context);
            REN_SERVICE_CLIENT = new RENServiceClientImpl(myHTTPHandler);
        }

        return REN_SERVICE_CLIENT;
    }

    public static FileDownloader getFileDownloader() {
        if (FILE_DOWNLOADER == null) {
            FILE_DOWNLOADER = new FileDownloaderImpl();
        }

        return FILE_DOWNLOADER;
    }

    public static DatabaseUtility getDatabaseUtility(final Context context) {
        if (DATABASE_UTILITY == null) {
            DATABASE_UTILITY = new DatabaseUtility(context);
        }

        return DATABASE_UTILITY;
    }

    public static DataLoader getPublishedDataloader(final Context context) {
        if (PUBLISHED_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseUtility databaseUtility = getDatabaseUtility(context);
            PUBLISHED_DATA_LOADER = new PublishedDataLoader(renServiceClient, databaseUtility);
        }

        return PUBLISHED_DATA_LOADER;
    }

    public static DataLoader getRecentDataloader(final Context context) {
        if (RECENT_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseUtility databaseUtility = getDatabaseUtility(context);
            RECENT_DATA_LOADER = new RecentDataLoader(renServiceClient, databaseUtility);
        }

        return RECENT_DATA_LOADER;
    }

    public static DataLoader getTopMostDataloader(final Context context) {
        if (TOP_MOST_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseUtility databaseUtility = getDatabaseUtility(context);
            TOP_MOST_DATA_LOADER = new TopMostDataLoader(renServiceClient, databaseUtility);
        }

        return PUBLISHED_DATA_LOADER;
    }

    public static DataLoader getSavedDataloader(final Context context) {
        if (SAVED_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseUtility databaseUtility = getDatabaseUtility(context);
            SAVED_DATA_LOADER = new SavedDataLoader(databaseUtility);
        }

        return SAVED_DATA_LOADER;
    }
}
