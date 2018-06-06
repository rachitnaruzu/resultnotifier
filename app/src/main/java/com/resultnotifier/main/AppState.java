package com.resultnotifier.main;

import android.content.Context;

import com.resultnotifier.main.db.DatabaseManager;
import com.resultnotifier.main.downloader.FileDownloader;
import com.resultnotifier.main.downloader.FileDownloaderImpl;
import com.resultnotifier.main.loader.DataLoader;
import com.resultnotifier.main.loader.PublishedDataLoader;
import com.resultnotifier.main.loader.RecentDataLoader;
import com.resultnotifier.main.loader.SavedDataLoader;
import com.resultnotifier.main.loader.TopMostDataLoader;
import com.resultnotifier.main.service.MyHTTPHandler;
import com.resultnotifier.main.service.RENServiceClient;
import com.resultnotifier.main.service.RENServiceClientImpl;

public class AppState {
    private static RENServiceClient REN_SERVICE_CLIENT;
    private static FileDownloader FILE_DOWNLOADER;
    private static DataLoader PUBLISHED_DATA_LOADER;
    private static DataLoader RECENT_DATA_LOADER;
    private static DataLoader SAVED_DATA_LOADER;
    private static DataLoader TOP_MOST_DATA_LOADER;
    private static DatabaseManager DATABASE_MANAGER;

    public static RENServiceClient getRenServiceClient(final Context context) {
        if (REN_SERVICE_CLIENT == null) {
            final MyHTTPHandler myHTTPHandler = new MyHTTPHandler(context);
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

    public static DatabaseManager getDatabaseManager(final Context context) {
        if (DATABASE_MANAGER == null) {
            DATABASE_MANAGER = new DatabaseManager(context);
        }

        return DATABASE_MANAGER;
    }

    public static DataLoader getPublishedDataLoader(final Context context) {
        if (PUBLISHED_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseManager databaseManager = getDatabaseManager(context);
            PUBLISHED_DATA_LOADER = new PublishedDataLoader(renServiceClient, databaseManager);
        }

        return PUBLISHED_DATA_LOADER;
    }

    public static DataLoader getRecentDataLoader(final Context context) {
        if (RECENT_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseManager databaseManager = getDatabaseManager(context);
            RECENT_DATA_LOADER = new RecentDataLoader(renServiceClient, databaseManager);
        }

        return RECENT_DATA_LOADER;
    }

    public static DataLoader getTopMostDataLoader(final Context context) {
        if (TOP_MOST_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseManager databaseManager = getDatabaseManager(context);
            TOP_MOST_DATA_LOADER = new TopMostDataLoader(renServiceClient, databaseManager);
        }

        return PUBLISHED_DATA_LOADER;
    }

    public static DataLoader getSavedDataLoader(final Context context) {
        if (SAVED_DATA_LOADER == null) {
            final RENServiceClient renServiceClient = getRenServiceClient(context);
            final DatabaseManager databaseManager = getDatabaseManager(context);
            SAVED_DATA_LOADER = new SavedDataLoader(databaseManager);
        }

        return SAVED_DATA_LOADER;
    }
}
