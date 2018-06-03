package com.resultnotifier.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.resultnotifier.main.service.RENServiceClient;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class DownloadJob {

    private final FileData mFile;
    private final AsyncTask<String, Integer, Long> mDownloadFileTask;
    private File mFinalFile;
    private File mTempFile;
    private Exception mException;
    private RENServiceClient mRenServiceClient;

    public DownloadJob(final Activity mainActivity,
                       final RENServiceClient renServiceClient,
                       final FileData filedata,
                       final MyAdaptor mListAdapter) {
        mFile = filedata;
        mTempFile = filedata.getTempFile();
        mException = null;
        mRenServiceClient = renServiceClient;

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/xmls");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.mTempFile = new File(dir, mFile.getFileId() + ".temp");
        this.mFinalFile = new File(dir, mFile.getFileId() + "." + mFile.getFileType());

        mDownloadFileTask = new AsyncTask<String, Integer, Long>() {

            protected Long doInBackground(String... downloadlinks) {
                try {
                    URL url = new URL(downloadlinks[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int progressStatus;
                    int lengthoffile = connection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream output = new FileOutputStream(mTempFile);
                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        progressStatus = (int) ((total * 100) / lengthoffile);
                        publishProgress(progressStatus);
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                } catch (Exception e) {
                    Log.e("DownloadJob: ", e.getMessage());
                    e.printStackTrace();
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mFile.setProgress(0);
                mFile.setInProcess(true);
            }

            protected void onPostExecute(Long result) {
                if (mException != null) {
                    MainActivity.getmSnackbar().setText("File Download Error");
                    MainActivity.getmSnackbar().show();
                    mFile.setProgress(0);
                    mFile.setInProcess(false);
                    mListAdapter.notifyDataSetChanged();
                    return;
                }
                mFile.setProgress(100);

                mTempFile.renameTo(mFinalFile);
                mFinalFile = mTempFile;
                mFile.setIsCompleted(true);
                mFile.setInProcess(false);
                DatabaseUtility dbUtil = DatabaseUtility.getInstance(mainActivity.getApplicationContext());
                if (!dbUtil.isFilePresent(filedata.getFileId())) {
                    dbUtil.addFileData(mFile);
                }
                incrementViewsByOne(mFile.getFileId());
                mListAdapter.notifyDataSetChanged();
            }

            protected void onProgressUpdate(Integer... progress) {
                mFile.setProgress(progress[0]);
            }
        };
        mDownloadFileTask.execute(filedata.getUrl());
    }

    private void incrementViewsByOne(final String fileId) {
        mRenServiceClient.incrementViewsByOne(fileId, null);
    }
}