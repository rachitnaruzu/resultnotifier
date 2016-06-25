package com.resultnotifier.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

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

    private FileData file;
    private File finalFile, tempFile;
    private Activity mainActivity;
    private ImageView tick;
    private AsyncTask<String, Integer, Long> downloadFileTask;
    private boolean isAlive;
    private String global_fileid;
    private MyAdaptor mListAdapter;
    private Exception ex;

    public boolean is_Alive() {
        return isAlive;
    }

    public void incrementViewsByOne(String fileid){
        global_fileid = fileid;
        Map<String, String> params = new HashMap<>();
        params.put("fileid", fileid);
        params = MainFragment.addSecureParams(params);
        CustomRequest updateSelfViewsRequest = new CustomRequest(Request.Method.POST, MainFragment.INCREMENT_VIEW_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //DatabaseUtility.getInstance(mainActivity.getApplicationContext()).incrementViewsByOne(global_fileid);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                DatabaseUtility.getInstance(mainActivity.getApplicationContext()).incrementSelfViewsByOne(global_fileid);
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        MyHTTPHandler.getInstance(mainActivity.getApplicationContext()).addToRequestQueue(updateSelfViewsRequest);
    }

    public DownloadJob(final Activity mainActivityy, final FileData filedata, final MyAdaptor mListAdapter) {
        this.file = filedata;
        this.mListAdapter = mListAdapter;
        this.tempFile = filedata.tempFile;
        this.mainActivity = mainActivityy;
        this.ex = null;

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/xmls");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        this.tempFile = new File(dir, file.fileid + ".temp");
        this.finalFile = new File(dir, file.fileid + "." + file.filetype);

        downloadFileTask = new AsyncTask<String, Integer, Long>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                file.setProgress(0);
                file.setInProcess(true);
                isAlive = true;
            }

            protected Long doInBackground(String... downloadlinks) {
                try {
                    URL url = new URL(downloadlinks[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int progressStatus;
                    int lengthoffile = connection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream output = new FileOutputStream(tempFile);
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
                    ex = e;
                }
                return null;
            }

            protected void onProgressUpdate(Integer... progress) {
                file.setProgress(progress[0]);
            }

            protected void onPostExecute(Long result) {
                if(ex != null) {
                    MainActivity.getmSnackbar().setText("File Download Error");
                    MainActivity.getmSnackbar().show();
                    file.setProgress(0);
                    file.setInProcess(false);
                    mListAdapter.notifyDataSetChanged();
                    return;
                }
                file.setProgress(100);

                tempFile.renameTo(finalFile);
                finalFile = tempFile;
                file.iscompleted = true;
                file.setInProcess(false);
                isAlive = false;
                DatabaseUtility dbUtil = DatabaseUtility.getInstance(mainActivityy.getApplicationContext());
                if(!dbUtil.isFilePresent(filedata.fileid)) {
                    dbUtil.addFileData(file);
                }
                incrementViewsByOne(file.fileid);
                mListAdapter.notifyDataSetChanged();
            }
        };
        downloadFileTask.execute(filedata.url);
    }
}