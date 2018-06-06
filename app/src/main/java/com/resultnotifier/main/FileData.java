package com.resultnotifier.main;

import android.view.View;
import android.widget.ProgressBar;

import com.resultnotifier.main.ui.main.MainActivity;

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
    private boolean mIsDownloadInProcess;
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

    public boolean isDownloadInProcess() {
        return mIsDownloadInProcess;
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

    public FileData() {
        mIsCompleted = false;
        mIsSelected = false;
        mIsDisplaySelected = false;
        mSelfViews = "0";
        mViews = "0";
        mColor = MainActivity.getRandomColor();
        mProgressBar = null;
        mIsDownloadInProcess = false;
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

    public void setDownloadInProcess(final boolean inProcess) {
        this.mIsDownloadInProcess = inProcess;
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