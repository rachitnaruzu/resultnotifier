package com.resultnotifier.main.downloader;

public interface FileDownloader {
    void downloadFile(final String fileUrl, final String fileName,
                      final DownloadFileCallback downloadFileCallback);

    interface DownloadFileCallback {
        void onDownloadStart();
        void onProgressUpdate(final int progress);
        void onDownloadComplete();
        void onDownloadFail(final int error);
    }
}
