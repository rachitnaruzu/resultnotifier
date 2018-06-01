package com.resultnotifier.main;

import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

public class FileData {
    public String mDisplayName;
    public String mFileType;
    public String mFileId;
    public String mDateCreated;
    public String mDataType;
    public String mUrl;
    public String mViews;
    public String selfViews;
    int color;
    public boolean mIsCompleted;
    boolean displaySelected;
    boolean isSelected;
    DownloadJob downloadjob;
    File tempFile;
    private boolean inProcess;
    private ProgressBar progressBar;

    public FileData() {
        mIsCompleted = false;
        isSelected = false;
        displaySelected = false;
        selfViews = mViews = "0";
        //this.vi = null;
        color = MainActivity.getRandomColor();
        progressBar = null;
        inProcess = false;
    }

    public FileData(String displayName, String fileType, String fileId, String dateCreated,
                    String dataType, String views, String url) {
        mDisplayName = displayName;
        mFileType = fileType;
        mFileId = fileId;
        mDateCreated = dateCreated;
        mDataType = dataType;
        mViews = views;
        mUrl = url;
        mIsCompleted = false;
    }

    public boolean getInProcess() {
        return inProcess;
    }

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            if (progressBar.getVisibility() == View.INVISIBLE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            progressBar.setProgress(progress);
        }
    }
}