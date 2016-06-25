package com.resultnotifier.main;

import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

public class FileData {
    String displayname;
    String filetype;
    String fileid;
    String datecreated;
    String datatype;
    String url;
    String views;
    String selfViews;
    int color;
    boolean iscompleted;
    boolean displaySelected;
    boolean isSelected;
    DownloadJob downloadjob;
    File finalFile;
    File tempFile;
    //View vi;
    private int progress;
    private boolean inProcess;
    private ProgressBar progressBar;

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }

    public boolean getInProcess() {
        return inProcess;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setProgress(int progress){
        this.progress = progress;
        if(progressBar != null) {
            if(progressBar.getVisibility() == View.INVISIBLE){
                progressBar.setVisibility(View.VISIBLE);
            }
            progressBar.setProgress(progress);
        }
    }

    public int getProgress() {
        return progress;
    }

    public boolean is_file_downloaded(){
        return iscompleted;
    }

    public FileData() {
        iscompleted = false;
        isSelected = false;
        displaySelected = false;
        selfViews = views = "0";
        //this.vi = null;
        color = MainActivity.getRandomColor();
        progress = 0;
        progressBar = null;
        inProcess = false;
    }

    public FileData(String displayname, String filetype, String fileid, String datecreated, String datatype, String views, String url) {
        this.displayname = displayname;
        this.fileid = fileid;
        this.iscompleted = false;
        this.filetype = filetype;
        this.datecreated = datecreated;
        this.datatype = datatype;
        this.views = views;
        this.url = url;
    }
}