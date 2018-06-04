package com.resultnotifier.main.downloader;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloaderImpl implements FileDownloader {
    private static final String TAG = "REN_FileDownloaderImpl";
    public static final int DIRECTORY_CREATION_FAILED = 101;
    public static final int FILE_DOWNLOAD_FAILED = 102;
    public static final int FILE_RENAME_FAILED = 103;

    @Override
    public void downloadFile(final String fileUrl, final String fileName,
                             final DownloadFileCallback downloadFileCallback) {
        new FileDownloadTask(fileUrl, fileName, downloadFileCallback).download();
    }

    private class FileDownloadTask extends AsyncTask<String, Integer, Long> {
        private static final String TEMP = "temp";
        private static final String DOWNLOAD_DIRECTORY = "xmls";

        private final String mFileDownloadUrl;
        private final String mFileName;
        private final DownloadFileCallback mDownloadFileCallback;
        private File mTempFile;
        private File mFinalFile;

        FileDownloadTask(final String fileDownloadUrl,
                         final String fileName,
                         final DownloadFileCallback downloadFileCallback) {
            mFileDownloadUrl = fileDownloadUrl;
            mFileName = fileName;
            mDownloadFileCallback = downloadFileCallback;
        }

        void download() {
            final File root = android.os.Environment.getExternalStorageDirectory();
            final File dir = new File(root.getAbsolutePath()
                    + "/" + DOWNLOAD_DIRECTORY);
            if (!dir.exists()) {
                if(dir.mkdirs()){
                    Log.i(TAG, "Created directory to store downloaded files");
                } else {
                    Log.e(TAG, "Unable to open directory=" + dir.getAbsolutePath());
                    mDownloadFileCallback.onDownloadFail(DIRECTORY_CREATION_FAILED);
                }
            }

            mTempFile = new File(dir, mFileName + "." + TEMP);
            mFinalFile = new File(dir, mFileName);

            Log.i(TAG, "Starting file download with URL=" + mFileDownloadUrl);
            execute(mFileDownloadUrl);
        }

        @Override
        public Long doInBackground(final String... downloadLinks) {
            final URL url;
            try {
                url = new URL(downloadLinks[0]);
            } catch (final MalformedURLException e) {
                Log.e(TAG, "Invalid download URL=" + downloadLinks[0]);
                return null;
            }

            try {
                final InputStream input = new BufferedInputStream(url.openStream(), 8192);
                final OutputStream output = new FileOutputStream(mTempFile);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                int progressStatus;
                final URLConnection connection = url.openConnection();
                connection.connect();
                final int lengthOfFile = connection.getContentLength();
                while ((count = input.read(data)) != -1) {
                    total += count;
                    progressStatus = (int) ((total * 100) / lengthOfFile);
                    mDownloadFileCallback.onProgressUpdate(progressStatus);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (final IOException e) {
                Log.e(TAG, "Unable to download file, file nme=" + mFileName, e);
                mDownloadFileCallback.onDownloadFail(FILE_DOWNLOAD_FAILED);
            }

            return null;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "Downloading started. file name=" + mFileName);
            mDownloadFileCallback.onDownloadStart();
        }

        @Override
        public void onPostExecute(final Long result) {
            if(mTempFile.renameTo(mFinalFile)) {
                Log.i(TAG, "Downloading completed. file name=" + mFileName);
                mDownloadFileCallback.onDownloadComplete();
            } else {
                Log.e(TAG, "Unable to rename file. file name=" + mFileName);
                mDownloadFileCallback.onDownloadFail(FILE_RENAME_FAILED);
            }
        }
    }
}
