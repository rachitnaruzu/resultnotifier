package com.resultnotifier.main.ui;

import android.widget.AbsListView;

public interface MultiFileSelector extends AbsListView.MultiChoiceModeListener {
    void exitActionMode();

    interface MultiFileSelectorCallback {
        void deleteSelectedFiles();
        void deSelect();
    }
}
