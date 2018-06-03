package com.resultnotifier.main;

import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

public class FileData {
    private String mDisplayName;
    private String mFileType;
    private String mFileId;
    private String mDateCreated;
    private String mDataType;
    private String mUrl;
    private String mViews;
    private String mSelfViews;
    private int mColor;
    private boolean mIsCompleted;
    private boolean mIsDisplaySelected;
    private boolean mIsSelected;
    private DownloadJob mDownloadJob;
    private File mTempFile;
    private boolean mInProcess;
    private ProgressBar mProgressBar;

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getFileType() {
        return mFileType;
    }

    public String getFileId() {
        return mFileId;
    }

    public String getDateCreated() {
        return mDateCreated;
    }

    public String getDataType() {
        return mDataType;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getViews() {
        return mViews;
    }

    public String getSelfViews() {
        return mSelfViews;
    }

    public int getColor() {
        return mColor;
    }

    public boolean isCompleted() {
        return mIsCompleted;
    }

    public boolean isDisplaySelected() {
        return mIsDisplaySelected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public DownloadJob getDownloadjob() {
        return mDownloadJob;
    }

    public File getTempFile() {
        return mTempFile;
    }

    public boolean isInProcess() {
        return mInProcess;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setDisplayName(final String displayName) {
        mDisplayName = displayName;
    }

    public void setFileType(final String fileType) {
        mFileType = fileType;
    }

    public void setFileId(final String fileId) {
        mFileId = fileId;
    }

    public void setDateCreated(final String dateCreated) {
        mDateCreated = dateCreated;
    }

    public void setDataType(final String dataType) {
        mDataType = dataType;
    }

    public void setUrl(final String url) {
        mUrl = url;
    }

    public void setViews(final String views) {
        mViews = views;
    }

    public void setSelfViews(final String selfViews) {
        mSelfViews = selfViews;
    }

    public void setColor(final int color) {
        mColor = color;
    }

    public void setIsCompleted(final boolean isCompleted) {
        mIsCompleted = isCompleted;
    }

    public void setIsDisplaySelected(boolean isDisplaySelected) {
        mIsDisplaySelected = isDisplaySelected;
    }

    public void setIsSelected(final boolean isSelected) {
        mIsSelected = isSelected;
    }

    public void setDownloadjob(final DownloadJob downloadJob) {
        mDownloadJob = downloadJob;
    }

    public void setTempFile(final File tempFile) {
        mTempFile = tempFile;
    }

    public FileData() {
        mIsCompleted = false;
        mIsSelected = false;
        mIsDisplaySelected = false;
        mSelfViews = "0";
        mViews = "0";
        mColor = MainActivity.getRandomColor();
        mProgressBar = null;
        mInProcess = false;
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

    public void setInProcess(final boolean inProcess) {
        this.mInProcess = inProcess;
    }

    public void setProgressBar(final ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }

    public void setProgress(final int progress) {
        if (mProgressBar != null) {
            if (mProgressBar.getVisibility() == View.INVISIBLE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(progress);
        }
    }
}