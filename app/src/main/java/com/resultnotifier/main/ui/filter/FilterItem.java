package com.resultnotifier.main.ui.filter;

public class FilterItem {
    private String mDataType;
    private boolean mIsChecked;

    public String getDataType() {
        return mDataType;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setIsChecked(final boolean isChecked) {
        mIsChecked = isChecked;
    }

    public FilterItem(final String dataType, final boolean isChecked) {
        mDataType = dataType;
        mIsChecked = isChecked;
    }
}
