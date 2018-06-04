package com.resultnotifier.main;

import android.content.Context;
import android.os.Handler;

import com.resultnotifier.main.downloader.FileDownloader;
import com.resultnotifier.main.downloader.FileDownloaderImpl;
import com.resultnotifier.main.service.RENServiceClient;
import com.resultnotifier.main.service.RENServiceClientImpl;

public class AppState {
    private static RENServiceClient REN_SERVICE_CLIENT;
    private static FileDownloader FILE_DOWNLOADER;

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
}
